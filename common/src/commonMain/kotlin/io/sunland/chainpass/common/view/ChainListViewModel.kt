package io.sunland.chainpass.common.view

import androidx.compose.runtime.mutableStateListOf
import io.sunland.chainpass.common.Chain
import io.sunland.chainpass.common.ChainStatus
import io.sunland.chainpass.common.repository.ChainEntity
import io.sunland.chainpass.common.repository.ChainKeyEntity
import io.sunland.chainpass.common.repository.ChainRepository
import io.sunland.chainpass.common.security.PasswordEncoder

class ChainListViewModel(private val repository: ChainRepository) {
    val chains = mutableStateListOf<Chain>()

    suspend fun getAll(): Result<Unit> {
        return repository.read().map { chainEntities ->
            this.chains.clear()
            this.chains.addAll(chainEntities.map { chainEntity ->
                Chain().apply {
                    id = chainEntity.id
                    name = Chain.Name(chainEntity.name)
                    status = ChainStatus.ACTUAL
                }
            })
        }
    }

    fun draft() {
        val chain = Chain()

        chains.add(chain)
    }

    fun rejectDraft(chain: Chain) {
        chains.remove(chain)
    }

    suspend fun new(chain: Chain): Result<Unit> {
        chain.key = Chain.Key(PasswordEncoder.hash(chain.key.value, chain.name.value))

        val chainEntity = ChainEntity(chain.id, chain.name.value, chain.key.value)

        return repository.create(chainEntity).map { chainId ->
            chain.id = chainId
            chain.key = Chain.Key()
            chain.status = ChainStatus.ACTUAL

            val chains = chains.toList()

            this.chains.clear()
            this.chains.addAll(chains)
        }
    }

    fun remove(chain: Chain, chainKey: Chain.Key) = Chain().apply {
        id = chain.id
        name = chain.name
        key = Chain.Key(PasswordEncoder.hash(chainKey.value, chain.name.value))
        status = chain.status

        chains.remove(chain)
    }

    fun undoRemove(chain: Chain) {
        chain.key = Chain.Key()

        chains.add(chain)
    }

    suspend fun remove(chain: Chain): Result<Unit> {
        val chainKeyEntity = ChainKeyEntity(chain.id, chain.key.value)

        return repository.delete(chainKeyEntity)
    }

    fun select(chain: Chain, chainKey: Chain.Key) = Chain().apply {
        id = chain.id
        name = chain.name
        key = Chain.Key(PasswordEncoder.hash(chainKey.value, chain.name.value))
        status = chain.status
    }
}
