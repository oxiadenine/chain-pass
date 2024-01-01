package io.sunland.chainpass.common.view

import androidx.compose.runtime.mutableStateListOf
import io.sunland.chainpass.common.repository.Chain
import io.sunland.chainpass.common.repository.ChainLink
import io.sunland.chainpass.common.repository.ChainLinkRepository

class ChainLinkListViewModel(private val repository: ChainLinkRepository) : ViewModel {
    var chain: ChainListItem? = null

    val chainLinks = mutableStateListOf<ChainLinkListItem>()

    override fun refresh() {
        val chainLinks = chainLinks.toList()

        this.chainLinks.clear()
        this.chainLinks.addAll(chainLinks)
    }

    suspend fun new(chainLink: ChainLinkListItem): Result<Unit> {
        return repository.create(ChainLink(chainLink.id, chainLink.name, chainLink.password, chainLink.chainId))
            .map { chainLinkId -> chainLink.id = chainLinkId }
    }

    suspend fun getAll(): Result<Unit> {
        val chain = Chain(chain!!.id, chain!!.name, chain!!.key)

        return repository.read(chain).map { chainLinks ->
            this.chainLinks.clear()
            this.chainLinks.addAll(chainLinks.map { chainLink ->
                ChainLinkListItem(
                    chainLink.id,
                    chainLink.name,
                    chainLink.password,
                    chainLink.chainId,
                    ChainLinkListItemStatus.ACTUAL
                )
            })
        }
    }

    suspend fun edit(chainLink: ChainLinkListItem): Result<Unit> {
        return repository.update(ChainLink(chainLink.id, chainLink.name, chainLink.password, chainLink.chainId))
    }

    suspend fun remove(chainLink: ChainLinkListItem): Result<Unit> {
        return repository.delete(ChainLink(chainLink.id, chainLink.name, chainLink.password, chainLink.chainId))
    }
}
