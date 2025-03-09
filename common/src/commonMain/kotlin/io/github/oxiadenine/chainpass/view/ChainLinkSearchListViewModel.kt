package io.github.oxiadenine.chainpass.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import io.github.oxiadenine.chainpass.Chain
import io.github.oxiadenine.chainpass.ChainLink
import io.github.oxiadenine.chainpass.repository.ChainLinkRepository
import io.github.oxiadenine.chainpass.repository.ChainRepository

class ChainLinkSearchListViewModel(
    private val chainRepository: ChainRepository,
    private val chainLinkRepository: ChainLinkRepository
) {
    private var chainLinks = emptyList<ChainLink>()

    val chainLinkSearchListState = chainLinks.toMutableStateList()

    fun search(keyword: String) {
        val chainLinks = chainLinks
            .filter { chainLink -> chainLink.name.value.lowercase().contains(keyword.lowercase()) }
            .sortedBy { chainLink -> chainLink.name.value }

        chainLinkSearchListState.clear()
        chainLinkSearchListState.addAll(chainLinks)
    }

    suspend fun getAll(chainId: String) {
        val chain = chainRepository.getOne(chainId).getOrThrow().let { chainEntity ->
            Chain().apply {
                id = chainEntity.id
                name = Chain.Name(chainEntity.name)
                key = Chain.Key(chainEntity.key)
                salt = chainEntity.salt
            }
        }

        chainLinks = chainLinkRepository.getBy(chain.id).map { chainLinkEntity ->
            ChainLink(chain).apply {
                id = chainLinkEntity.id
                name = ChainLink.Name(chainLinkEntity.name)
                description = ChainLink.Description(chainLinkEntity.description)
                password = ChainLink.Password(chainLinkEntity.password)
                iv = chainLinkEntity.iv
            }
        }

        chainLinkSearchListState.clear()
        chainLinkSearchListState.addAll(chainLinks)
    }
}

@Composable
fun rememberChainLinkSearchListViewModel(
    chainRepository: ChainRepository,
    chainLinkRepository: ChainLinkRepository
) = remember {
    ChainLinkSearchListViewModel(chainRepository, chainLinkRepository)
}