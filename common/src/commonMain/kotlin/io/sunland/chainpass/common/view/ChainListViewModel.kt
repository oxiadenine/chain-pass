package io.sunland.chainpass.common.view

import androidx.compose.runtime.mutableStateListOf
import io.sunland.chainpass.common.repository.Chain
import io.sunland.chainpass.common.repository.ChainRepository

class ChainListViewModel(private val repository: ChainRepository) : ViewModel {
    val chains = mutableStateListOf<ChainListItem>()

    override fun refresh() {
        val chains = chains.toList()

        this.chains.clear()
        this.chains.addAll(chains)
    }

    suspend fun new(chain: ChainListItem): Result<Unit> {
        return repository.create(Chain(chain.id, chain.name, chain.key)).map { chainId -> chain.id = chainId }
    }

    suspend fun getAll(): Result<Unit> {
        return repository.read().map { chains ->
            this.chains.clear()
            this.chains.addAll(chains.map { chain ->
                ChainListItem(chain.id, chain.name, chain.key, ChainListItemStatus.ACTUAL)
            })
        }
    }

    suspend fun remove(chain: ChainListItem): Result<Unit> {
        return repository.delete(Chain(chain.id, chain.name, chain.key))
    }
}
