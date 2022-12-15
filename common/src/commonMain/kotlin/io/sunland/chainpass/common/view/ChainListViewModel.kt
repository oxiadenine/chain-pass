package io.sunland.chainpass.common.view

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import io.sunland.chainpass.common.Chain
import io.sunland.chainpass.common.ChainLink
import io.sunland.chainpass.common.Storable
import io.sunland.chainpass.common.Storage
import io.sunland.chainpass.common.network.*
import io.sunland.chainpass.common.security.EncoderSpec
import io.sunland.chainpass.common.security.PasswordEncoder

class ChainListViewModel(private val chainApi: ChainApi, private val chainLinkApi: ChainLinkApi) {
    val chainListState = mutableStateListOf<Chain>()

    val chainState = mutableStateOf<Chain?>(null)

    val chainLatestIndex: Int
        get() {
            return chainListState.indexOfFirst { chain -> chain.isLatest }
        }

    private var chains = emptyList<Chain>()

    fun draft() {
        val chainDraft = Chain().apply {
            id = chains.plus(chainListState.filter { chain ->
                chain.status == Chain.Status.DRAFT
            }).maxOfOrNull { chain -> chain.id }?.let { it + 1 } ?: 1
            isLatest = true
        }

        chainListState.add(chainDraft)

        val chains = chainListState.map { chain ->
            if (chain.id != chainDraft.id) {
                chain.apply { isLatest = false }
            } else chain
        }

        chainListState.clear()
        chainListState.addAll(chains)
    }

    fun rejectDraft(chain: Chain) {
        chainListState.remove(chain)
    }

    fun removeLater(chain: Chain) {
        chainListState.remove(chain)
    }

    fun undoRemove(chainRemove: Chain) {
        val chainsDraft = chainListState.filter { chain -> chain.status == Chain.Status.DRAFT }

        val chains = chainListState
            .filter { chain -> chain.status != Chain.Status.DRAFT }
            .plus(chainRemove.apply { key = Chain.Key() })
            .sortedBy { chain -> chain.name.value }
            .plus(chainsDraft)

        chainListState.clear()
        chainListState.addAll(chains)
    }

    suspend fun store(chain: Chain, storage: Storage) = runCatching {
        val secretKey = PasswordEncoder.hash(EncoderSpec.Passphrase(
            PasswordEncoder.Base64.encode(chain.key.value.encodeToByteArray()),
            PasswordEncoder.Base64.encode(chain.name.value.encodeToByteArray())
        ))

        val privateKey = PasswordEncoder.encrypt(
            PasswordEncoder.Base64.encode(chain.key.value.encodeToByteArray()),
            EncoderSpec.Passphrase(secretKey, PasswordEncoder.Base64.encode(chain.name.value.encodeToByteArray()))
        )

        val chainLinks = chainApi.key(chain.id).mapCatching { chainKeyEntity ->
            val saltKey = PasswordEncoder.hash(EncoderSpec.Passphrase(privateKey, chainKeyEntity.key))

            chainLinkApi.read(ChainKeyEntity(chain.id, saltKey)).getOrThrow()
        }.map { chainLinkEntities ->
            chainLinkEntities.map { chainLinkEntity ->
                ChainLink(chain).apply {
                    id = chainLinkEntity.id
                    name = ChainLink.Name(chainLinkEntity.name)
                    description = ChainLink.Description(chainLinkEntity.description)
                    password = ChainLink.Password(chainLinkEntity.password)
                }
            }
        }.getOrThrow()

        val storable = Storable(
            mapOf("isPrivate" to storage.options.isPrivate.toString()),
            mapOf("name" to chain.name.value, "key" to privateKey),
            chainLinks.map { chainLink ->
                mapOf(
                    "name" to chainLink.name.value,
                    "description" to chainLink.description.value,
                    "password" to if (!storage.options.isPrivate) {
                        chainLink.unlockPassword().value
                    } else chainLink.password.value
                )
            }
        )

        storage.store(storable)
    }

    suspend fun unstore(chainKey: Chain.Key, storage: Storage, filePath: FilePath) = runCatching {
        val storable = storage.unstore(filePath.value)

        val chain = Chain().apply {
            id = chains.plus(chainListState.filter { chain ->
                chain.status == Chain.Status.DRAFT
            }).maxOfOrNull { chain -> chain.id }?.let { it + 1 } ?: 1
            name = Chain.Name(storable.chain["name"]!!)
            key = Chain.Key(storable.chain["key"]!!)
        }

        val secretKey = PasswordEncoder.hash(EncoderSpec.Passphrase(
            PasswordEncoder.Base64.encode(chainKey.value.encodeToByteArray()),
            PasswordEncoder.Base64.encode(chain.name.value.encodeToByteArray())
        ))

        val privateKey = PasswordEncoder.encrypt(
            PasswordEncoder.Base64.encode(chainKey.value.encodeToByteArray()),
            EncoderSpec.Passphrase(secretKey, PasswordEncoder.Base64.encode(chain.name.value.encodeToByteArray()))
        )

        chain.key.matches(privateKey).getOrThrow()

        val chainLinks = storable.chainLinks.mapIndexed { chainLinkId, chainLink ->
            ChainLink(chain).apply {
                id = chainLinkId + 1
                name = ChainLink.Name(chainLink["name"]!!)
                description = ChainLink.Description(chainLink["description"]!!)
                password = if (!storable.options["isPrivate"]!!.toBoolean()) {
                    val privatePassword = PasswordEncoder.encrypt(
                        PasswordEncoder.Base64.encode(chainLink["password"]!!.encodeToByteArray()),
                        EncoderSpec.Passphrase(secretKey, PasswordEncoder.Base64.encode(chainLink["name"]!!.encodeToByteArray()))
                    )

                    ChainLink.Password(privatePassword)
                } else ChainLink.Password(chainLink["password"]!!)
            }
        }

        val chainEntity = ChainEntity(chain.id, chain.name.value, chain.key.value)

        chainApi.create(chainEntity).mapCatching {
            chainApi.key(chainLinks[0].chain.id).mapCatching { chainKeyEntity ->
                val saltKey = PasswordEncoder.hash(EncoderSpec.Passphrase(privateKey, chainKeyEntity.key))

                val chainLinkEntities = chainLinks.map { chainLink ->
                    ChainLinkEntity(
                        chainLink.id,
                        chainLink.name.value,
                        chainLink.description.value,
                        chainLink.password.value,
                        ChainKeyEntity(chainLink.chain.id, saltKey)
                    )
                }

                chainLinkApi.create(chainLinkEntities).getOrThrow()
            }
        }.mapCatching {
            chain.key = Chain.Key()
            chain.status = Chain.Status.ACTUAL

            chainListState.add(chain)

            update()
        }.getOrThrow()
    }

    suspend fun getAll() = chainApi.read().map { chainEntities ->
        chains = chainEntities.map { chainEntity ->
            Chain().apply {
                id = chainEntity.id
                name = Chain.Name(chainEntity.name)
                status = Chain.Status.ACTUAL
            }
        }

        val chainsDraft = chainListState.filter { chain -> chain.status == Chain.Status.DRAFT }

        val chains = chains
            .map { chain -> Chain(chain) }
            .sortedBy { chain -> chain.name.value }
            .plus(chainsDraft)

        chainListState.clear()
        chainListState.addAll(chains)

        Unit
    }

    suspend fun new(chain: Chain): Result<Unit> {
        val secretKey = PasswordEncoder.hash(EncoderSpec.Passphrase(
            PasswordEncoder.Base64.encode(chain.key.value.encodeToByteArray()),
            PasswordEncoder.Base64.encode(chain.name.value.encodeToByteArray())
        ))

        val privateKey = PasswordEncoder.encrypt(
            PasswordEncoder.Base64.encode(chain.key.value.encodeToByteArray()),
            EncoderSpec.Passphrase(secretKey, PasswordEncoder.Base64.encode(chain.name.value.encodeToByteArray()))
        )

        val chainEntity = ChainEntity(chain.id, chain.name.value, privateKey)

        return chainApi.create(chainEntity).map {
            chain.key = Chain.Key()
            chain.status = Chain.Status.ACTUAL

            update()
        }
    }

    suspend fun remove(chain: Chain) = chainApi.key(chain.id).mapCatching { chainKeyEntity ->
        val secretKey = PasswordEncoder.hash(EncoderSpec.Passphrase(
            PasswordEncoder.Base64.encode(chain.key.value.encodeToByteArray()),
            PasswordEncoder.Base64.encode(chain.name.value.encodeToByteArray())
        ))

        val privateKey = PasswordEncoder.encrypt(
            PasswordEncoder.Base64.encode(chain.key.value.encodeToByteArray()),
            EncoderSpec.Passphrase(secretKey, PasswordEncoder.Base64.encode(chain.name.value.encodeToByteArray()))
        )

        val saltKey = PasswordEncoder.hash(EncoderSpec.Passphrase(privateKey, chainKeyEntity.key))

        chainApi.delete(ChainKeyEntity(chain.id, saltKey)).getOrThrow()

        update()
    }

    private fun update() {
        val chainsRemove = chains.filter { chain ->
            !chainListState.any { chainToFind -> chain.id == chainToFind.id }
        }

        chains = chainListState
            .filter { chain -> chain.status != Chain.Status.DRAFT }
            .map { chain -> Chain(chain) }
            .plus(chainsRemove)

        val chainsDraft = chainListState.filter { chain -> chain.status == Chain.Status.DRAFT }

        val chains = chainListState
            .filter { chain -> chain.status != Chain.Status.DRAFT }
            .sortedBy { chain -> chain.name.value }
            .plus(chainsDraft)

        chainListState.clear()
        chainListState.addAll(chains)
    }
}