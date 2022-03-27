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

    val chainRemoveState = mutableStateOf<Chain?>(null)
    val chainSelectState = mutableStateOf<Chain?>(null)

    private var chains = emptyList<Chain>()

    fun draft() {
        chainListState.add(Chain())
    }

    fun rejectDraft(chain: Chain) {
        chainListState.remove(chain)

        update()
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

    fun undoRemove(chain: Chain) {
        chainListState.add(chain.apply { key = Chain.Key() })

        update()
    }

    fun select(chainKey: Chain.Key) = chainSelectState.value?.let { chain ->
        Chain().apply {
            id = chain.id
            name = chain.name
            key = chainKey
            status = chain.status
        }
    }

    suspend fun getAll() = repository.read().map { chainEntities ->
        chains = chainEntities.map { chainEntity ->
            Chain().apply {
                id = chainEntity.id
                name = Chain.Name(chainEntity.name)
                status = Chain.Status.ACTUAL
            }
        }

        val draftChains = chainListState.filter { chain -> chain.status == Chain.Status.DRAFT }

        chainListState.clear()
        chainListState.addAll(chains.sortedBy { chain -> chain.name.value }.plus(draftChains))

        Unit
    }

    suspend fun new(chain: Chain): Result<Unit> {
        val secretKey = PasswordEncoder.hash(EncoderSpec.Passphrase(
            PasswordEncoder.Base64.encode(chain.key.value.encodeToByteArray()),
            PasswordEncoder.Base64.encode(chain.name.value.encodeToByteArray())
        ))

        val privateKey = PasswordEncoder.encrypt(EncoderSpec.Passphrase(
            secretKey,
            PasswordEncoder.Base64.encode(chain.name.value.encodeToByteArray())
        ), chain.key.value)

        val chainEntity = ChainEntity(chain.id, chain.name.value, privateKey)

        return repository.create(chainEntity).map { chainId ->
            chain.id = chainId
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

        val privateKey = PasswordEncoder.encrypt(EncoderSpec.Passphrase(
            secretKey,
            PasswordEncoder.Base64.encode(chain.name.value.encodeToByteArray())
        ), chain.key.value)

        val saltKey = PasswordEncoder.hash(EncoderSpec.Passphrase(privateKey, chainKeyEntity.key))

        repository.delete(ChainKeyEntity(chain.id, saltKey)).getOrThrow()

        update()
    }

    private fun update() {
        val draftChains = chainListState.filter { chain -> chain.status == Chain.Status.DRAFT }

        chains = chainListState.filter { chain -> chain.status != Chain.Status.DRAFT }

        chainListState.clear()
        chainListState.addAll(chains.sortedBy { chain -> chain.name.value }.plus(draftChains))
    }
}
