package io.sunland.chainpass.common.view

import androidx.compose.runtime.mutableStateListOf
import io.sunland.chainpass.common.Chain
import io.sunland.chainpass.common.ChainStatus
import io.sunland.chainpass.common.repository.ChainEntity
import io.sunland.chainpass.common.repository.ChainRepository

class ChainListViewModel(private val repository: ChainRepository) {
    val chains = mutableStateListOf<Chain>()

    suspend fun getAll(): Result<Unit> {
        return repository.read().map { chainEntities ->
            this.chains.clear()
            this.chains.addAll(chainEntities.map { chainEntity ->
                Chain().apply {
                    id = chainEntity.id
                    name = chainEntity.name
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
        chain.hashKey(chain.key.value)

        val chainEntity = ChainEntity(chain.id, chain.name, chain.key.value)

        return repository.create(chainEntity).map { chainId ->
            chain.id = chainId
            chain.status = ChainStatus.ACTUAL
            chain.key.clear()

            val chains = chains.toList()

            this.chains.clear()
            this.chains.addAll(chains)
        }
    }

    fun remove(chain: Chain, onRemove: (Chain) -> Unit) {
        chains.remove(chain)

        onRemove(chain)
    }

    fun undoRemove(chain: Chain) {
        chains.add(chain)
    }

    suspend fun remove(chain: Chain): Result<Unit> {
        val chainEntity = ChainEntity(chain.id, chain.name, chain.key.value)

        return repository.delete(chainEntity)
    }

    fun select(chain: Chain, key: String) = Chain().apply {
        id = chain.id
        name = chain.name

        setKey(key)
        hashKey(key)
    }
}
