package io.sunland.chainpass.common.view

import androidx.compose.runtime.mutableStateListOf
import io.sunland.chainpass.common.Chain
import io.sunland.chainpass.common.ChainLink
import io.sunland.chainpass.common.Storable
import io.sunland.chainpass.common.Storage
import io.sunland.chainpass.common.repository.ChainEntity
import io.sunland.chainpass.common.repository.ChainLinkEntity
import io.sunland.chainpass.common.repository.ChainLinkRepository
import io.sunland.chainpass.common.repository.ChainRepository

class ChainListViewModel(
    private val chainRepository: ChainRepository,
    private val chainLinkRepository: ChainLinkRepository,
    private val storage: Storage
) {
    val chainListState = mutableStateListOf<Chain>()

    val chainSelected: Chain?
        get() = chainListState.firstOrNull { chain -> chain.isSelected }

    val chainLatestIndex: Int
        get() = chainListState.indexOfFirst { chain -> chain.isLatest }

    private var chains = emptyList<Chain>()

    fun draft() {
        val chainDraft = Chain().apply {
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

    fun setSelected(chainSelect: Chain? = null) {
        val chainsDraft = chainListState.filter { chain -> chain.status == Chain.Status.DRAFT }

        val chains = chainListState
            .filter { chain -> chain.status != Chain.Status.DRAFT }
            .map { chain ->
                chain.isSelected = chain.id == chainSelect?.id

                chain
            }
            .sortedBy { chain -> chain.name.value }
            .plus(chainsDraft)

        chainListState.clear()
        chainListState.addAll(chains)
    }

    fun removeLater(chainRemove: Chain) {
        chainListState.removeIf { chain -> chain.id == chainRemove.id }
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

    fun getAll() = chainRepository.getAll().mapCatching { chainEntities ->
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

    fun new(chain: Chain) = runCatching {
        val secretKey = chain.secretKey()
        val privateKey = chain.privateKey(secretKey)

        val chainEntity = ChainEntity(chain.id, chain.name.value, privateKey.value)

        chainRepository.create(chainEntity).getOrThrow()

        chain.key = Chain.Key()
        chain.status = Chain.Status.ACTUAL

        update()
    }

    fun select(chain: Chain) = chainRepository.getOne(chain.id).mapCatching { chainEntity ->
        val secretKey = chain.secretKey()
        val privateKey = chain.privateKey(secretKey)

        Chain().apply {
            id = chainEntity.id
            name = Chain.Name(chainEntity.name)
            key = Chain.Key(chainEntity.key)
        }.validateKey(privateKey)
    }

    fun remove(chain: Chain) = chainRepository.getOne(chain.id).mapCatching { chainEntity ->
        val secretKey = chain.secretKey()
        val privateKey = chain.privateKey(secretKey)

        Chain().apply {
            id = chainEntity.id
            name = Chain.Name(chainEntity.name)
            key = Chain.Key(chainEntity.key)
        }.validateKey(privateKey)

        chainRepository.delete(chain.id).getOrThrow()

        update()
    }

    fun store(chain: Chain, storeOptions: StoreOptions) = chainRepository.getOne(chain.id).mapCatching { chainEntity ->
        val secretKey = chain.secretKey()
        val privateKey = chain.privateKey(secretKey)

        Chain().apply {
            id = chainEntity.id
            name = Chain.Name(chainEntity.name)
            key = Chain.Key(chainEntity.key)
        }.validateKey(privateKey)

        val chainLinks = chainLinkRepository.getBy(chain.id).map { chainLinkEntities ->
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
            mapOf("isPrivate" to storeOptions.isPrivate.toString()),
            mapOf("name" to chain.name.value, "key" to privateKey.value),
            chainLinks.map { chainLink ->
                mapOf(
                    "name" to chainLink.name.value,
                    "description" to chainLink.description.value,
                    "password" to if (!storeOptions.isPrivate) {
                        chainLink.plainPassword(secretKey).value
                    } else chainLink.password.value
                )
            }
        )

        storage.store(storable, storeOptions.type)
    }

    fun unstore(chainKey: Chain.Key, storage: Storage, filePath: FilePath) = runCatching {
        val storable = storage.unstore(filePath.value)

        val chain = Chain().apply {
            name = Chain.Name(storable.chain["name"]!!)
            key = chainKey
        }

        val secretKey = chain.secretKey()
        val privateKey = chain.privateKey(secretKey)

        chain.key = privateKey

        chain.validateKey(Chain.Key(storable.chain["key"]!!))

        val chainLinks = storable.chainLinks.map { chainLink ->
            ChainLink(chain).apply {
                name = ChainLink.Name(chainLink["name"]!!)
                description = ChainLink.Description(chainLink["description"]!!)
                password = ChainLink.Password(chainLink["password"]!!)
            }
        }.map { chainLink ->
            chainLink.password = if (!storable.options["isPrivate"]!!.toBoolean()) {
                chainLink.privatePassword(secretKey)
            } else chainLink.password

            chainLink
        }

        val chainEntity = ChainEntity(chain.id, chain.name.value, chain.key.value)

        chainRepository.create(chainEntity).map {
            val chainLinkEntities = chainLinks.map { chainLink ->
                ChainLinkEntity(
                    chainLink.id,
                    chainLink.name.value,
                    chainLink.description.value,
                    chainLink.password.value,
                    chainLink.chain.id
                )
            }

            chainLinkEntities.forEach { chainLinkEntity ->
                chainLinkRepository.create(chainLinkEntity)
            }
        }.getOrThrow()

        chain.key = Chain.Key()
        chain.status = Chain.Status.ACTUAL

        chainListState.add(chain)

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