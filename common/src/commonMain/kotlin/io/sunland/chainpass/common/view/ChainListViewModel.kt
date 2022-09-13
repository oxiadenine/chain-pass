package io.sunland.chainpass.common.view

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import io.sunland.chainpass.common.Chain
import io.sunland.chainpass.common.repository.ChainEntity
import io.sunland.chainpass.common.repository.ChainKeyEntity
import io.sunland.chainpass.common.repository.ChainRepository
import io.sunland.chainpass.common.security.EncoderSpec
import io.sunland.chainpass.common.security.PasswordEncoder

class ChainListViewModel(private val repository: ChainRepository) {
    val chainListState = mutableStateListOf<Chain>()

    val chainSelectState = mutableStateOf<Chain?>(null)
    val chainRemoveState = mutableStateOf<Chain?>(null)

    val chainLatestIndex: Int
        get() {
            return chainListState.indexOfFirst { chain -> chain.isLatest }
        }

    private var chains = emptyList<Chain>()

    fun draft() {
        val chainIds = chains.plus(chainListState.filter { chain ->
            chain.status == Chain.Status.DRAFT
        }).map { chain -> chain.id }

        val chainDraft = Chain().apply {
            id = chainIds.maxOrNull()?.let { it + 1 } ?: 1
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

    fun select(chainKey: Chain.Key) = chainSelectState.value?.let { chain ->
        Chain().apply {
            id = chain.id
            name = chain.name
            key = chainKey
            status = chain.status
        }
    }

    fun removeLater(chainKey: Chain.Key) = chainRemoveState.value?.let { chain ->
        chainListState.remove(chain)

        Chain().apply {
            id = chain.id
            name = chain.name
            key = chainKey
            status = chain.status
        }
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

    suspend fun getAll() = repository.read().map { chainEntities ->
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

        return repository.create(chainEntity).map {
            chain.key = Chain.Key()
            chain.status = Chain.Status.ACTUAL

            update()
        }
    }

    suspend fun remove(chain: Chain) = repository.key(chain.id).mapCatching { chainKeyEntity ->
        val secretKey = PasswordEncoder.hash(EncoderSpec.Passphrase(
            PasswordEncoder.Base64.encode(chain.key.value.encodeToByteArray()),
            PasswordEncoder.Base64.encode(chain.name.value.encodeToByteArray())
        ))

        val privateKey = PasswordEncoder.encrypt(
            PasswordEncoder.Base64.encode(chain.key.value.encodeToByteArray()),
            EncoderSpec.Passphrase(secretKey, PasswordEncoder.Base64.encode(chain.name.value.encodeToByteArray()))
        )

        val saltKey = PasswordEncoder.hash(EncoderSpec.Passphrase(privateKey, chainKeyEntity.key))

        repository.delete(ChainKeyEntity(chain.id, saltKey)).getOrThrow()

        update()
    }

    private fun update() {
        val chainsRemove = chains.filter { chain -> !chainListState.any { chain.id == it.id } }

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