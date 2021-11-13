package io.sunland.chainpass.common.view

import androidx.compose.runtime.mutableStateListOf
import io.sunland.chainpass.common.Chain
import io.sunland.chainpass.common.ChainLink
import io.sunland.chainpass.common.ChainLinkStatus
import io.sunland.chainpass.common.repository.ChainEntity
import io.sunland.chainpass.common.repository.ChainLinkEntity
import io.sunland.chainpass.common.repository.ChainLinkRepository

class ChainLinkListViewModel(private val repository: ChainLinkRepository) {
    var chain: Chain? = null

    val chainLinks = mutableStateListOf<ChainLink>()

    suspend fun getAll(): Result<Unit> {
        val chainEntity = ChainEntity(chain!!.id, chain!!.name, chain!!.key)

        return repository.read(chainEntity).map { chainLinkEntities ->
            this.chainLinks.clear()
            this.chainLinks.addAll(chainLinkEntities.map { chainLinkEntity ->
                ChainLink().apply {
                    id = chainLinkEntity.id
                    name = chainLinkEntity.name
                    password = chainLinkEntity.password
                    status = ChainLinkStatus.ACTUAL
                    chainId = chainLinkEntity.chainId
                }
            })
        }
    }

    fun draft() {
        val chainLink = ChainLink().apply {
            chainId = chain!!.id
        }

        chainLinks.add(chainLink)
    }

    fun rejectDraft(chainLink: ChainLink) {
        chainLinks.remove(chainLink)
    }

    suspend fun new(chainLink: ChainLink): Result<Unit> {
        val chainLinkEntity = ChainLinkEntity(chainLink.id, chainLink.name, chainLink.password, chainLink.chainId)

        return repository.create(chainLinkEntity).map { id ->
            chainLink.id = id
            chainLink.status = ChainLinkStatus.ACTUAL

            val chainLinks = chainLinks.toList()

            this.chainLinks.clear()
            this.chainLinks.addAll(chainLinks)
        }
    }
    
    fun startEdit(chainLink: ChainLink) {
        chainLink.status = ChainLinkStatus.EDIT

        val chainLinks = chainLinks.toList()

        this.chainLinks.clear()
        this.chainLinks.addAll(chainLinks)
    }

    fun endEdit(chainLink: ChainLink) {
        chainLink.status = ChainLinkStatus.ACTUAL

        val chainLinks = chainLinks.toList()

        this.chainLinks.clear()
        this.chainLinks.addAll(chainLinks)
    }

    suspend fun edit(chainLink: ChainLink): Result<Unit> {
        val chainLinkEntity = ChainLinkEntity(chainLink.id, chainLink.name, chainLink.password, chainLink.chainId)

        return repository.update(chainLinkEntity).onSuccess {
            this.endEdit(chainLink)
        }
    }

    fun remove(chainLink: ChainLink, onRemove: (ChainLink) -> Unit) {
        chainLinks.remove(chainLink)

        onRemove(chainLink)
    }

    fun undoRemove(chainLink: ChainLink) {
        chainLinks.add(chainLink)
    }

    suspend fun remove(chainLink: ChainLink): Result<Unit> {
        val chainLinkEntity = ChainLinkEntity(chainLink.id, chainLink.name, chainLink.password, chainLink.chainId)

        return repository.delete(chainLinkEntity)
    }
}
