package io.sunland.chainpass.common.view

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import io.sunland.chainpass.common.Chain
import io.sunland.chainpass.common.ChainLink
import io.sunland.chainpass.common.network.ChainApi
import io.sunland.chainpass.common.network.ChainKeyEntity
import io.sunland.chainpass.common.network.ChainLinkApi
import io.sunland.chainpass.common.network.ChainLinkEntity

class ChainLinkListViewModel(private val chainApi: ChainApi, private val chainLinkApi: ChainLinkApi) {
    val chainLinkListState = mutableStateListOf<ChainLink>()
    val chainLinkSearchListState = mutableStateListOf<ChainLink>()

    val isSearchState = mutableStateOf(false)
    val searchKeywordState = mutableStateOf("")

    val chainLinkLatestIndex: Int
        get() {
            return chainLinkListState.indexOfFirst { chainLink -> chainLink.isLatest }
        }

    var chain: Chain? = null
    var chainLinks = emptyList<ChainLink>()

    fun draft() {
        val chainLinkDraft = ChainLink(chain!!).apply {
            id = chainLinks.plus(chainLinkListState.filter { chainLink ->
                chainLink.status == ChainLink.Status.DRAFT
            }).maxOfOrNull { chainLink -> chainLink.id }?.let { it + 1 } ?: 1
            isLatest = true
        }

        chainLinkListState.add(chainLinkDraft)

        val chainLinks = chainLinkListState.map { chainLink ->
            if (chainLink.id != chainLinkDraft.id) {
                chainLink.apply { isLatest = false }
            } else chainLink
        }

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)
    }

    fun rejectDraft(chainLink: ChainLink) {
        chainLinkListState.remove(chainLink)
    }

    fun rejectDrafts() {
        val chainLinks = chainLinkListState.filter { chainLink -> chainLink.status != ChainLink.Status.DRAFT }

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)
    }

    fun startEdit(chainLinkEdit: ChainLink) {
        val chainLinksDraft = chainLinkListState.filter { chainLink -> chainLink.status == ChainLink.Status.DRAFT }

        val chainLinks = chainLinkListState
            .filter { chainLink -> chainLink.status != ChainLink.Status.DRAFT }
            .map { chainLink ->
                if (chainLink.id == chainLinkEdit.id) {
                    if (chainLink.password.isPrivate) {
                        val secretKey = chainLink.chain.secretKey()

                        chainLink.password = chainLink.plainPassword(secretKey)
                    }

                    chainLink.status = ChainLink.Status.EDIT
                }

                chainLink
            }
            .sortedBy { chainLink -> chainLink.name.value }
            .plus(chainLinksDraft)

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)
    }

    fun cancelEdit(chainLinkEdit: ChainLink) {
        val chainLinksDraft = chainLinkListState.filter { chainLink -> chainLink.status == ChainLink.Status.DRAFT }

        val chainLinks = chainLinkListState
            .filter { chainLink -> chainLink.status != ChainLink.Status.DRAFT }
            .map { chainLink ->
                if (chainLink.id == chainLinkEdit.id && chainLink.status == ChainLink.Status.EDIT) {
                    val chainLinkNoEdit = chainLinks.first { chainLinkToFind -> chainLink.id == chainLinkToFind.id }

                    chainLink.description = chainLinkNoEdit.description
                    chainLink.password = chainLinkNoEdit.password
                    chainLink.status = chainLinkNoEdit.status
                }

                chainLink.isLatest = false

                chainLink
            }
            .sortedBy { chainLink -> chainLink.name.value }
            .plus(chainLinksDraft)

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)
    }

    fun cancelEdits() {
        val chainLinksDraft = chainLinkListState.filter { chainLink -> chainLink.status == ChainLink.Status.DRAFT }

        val chainLinks = chainLinkListState
            .filter { chainLink -> chainLink.status != ChainLink.Status.DRAFT }
            .map { chainLink ->
                if (chainLink.status == ChainLink.Status.EDIT) {
                    val chainLinkNoEdit = chainLinks.first { chainLinkToFind -> chainLink.id == chainLinkToFind.id }

                    chainLink.description = chainLinkNoEdit.description
                    chainLink.password = chainLinkNoEdit.password
                    chainLink.status = chainLinkNoEdit.status
                }

                chainLink.isLatest = false

                chainLink
            }
            .sortedBy { chainLink -> chainLink.name.value }
            .plus(chainLinksDraft)

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)
    }

    fun removeLater(chainLinkRemove: ChainLink) {
        chainLinkListState.removeIf { chainLink -> chainLink.id == chainLinkRemove.id }
    }

    fun undoRemove(chainLinkRemove: ChainLink) {
        val chainLinksDraft = chainLinkListState.filter { chainLink -> chainLink.status == ChainLink.Status.DRAFT }

        val chainLinks = chainLinkListState
            .filter { chainLink -> chainLink.status != ChainLink.Status.DRAFT }
            .plus(chainLinkRemove)
            .sortedBy { chainLink -> chainLink.name.value }
            .plus(chainLinksDraft)

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)

        if (isSearchState.value) {
            search(searchKeywordState.value)
        }
    }

    fun copyPassword(chainLink: ChainLink): ChainLink.Password {
        val secretKey = chainLink.chain.secretKey()

        return chainLink.plainPassword(secretKey)
    }

    fun startSearch() {
        isSearchState.value = true
        searchKeywordState.value = ""

        val chainLinks = chainLinkListState
            .filter { chainLink -> chainLink.status == ChainLink.Status.ACTUAL }
            .sortedBy { chainLink -> chainLink.name.value }

        chainLinkSearchListState.addAll(chainLinks)
    }

    fun search(keyword: String) {
        searchKeywordState.value = keyword

        val chainLinks = chainLinkListState
            .filter { chainLink -> chainLink.status == ChainLink.Status.ACTUAL }
            .filter { chainLink -> chainLink.name.value.lowercase().contains(keyword.lowercase()) }
            .sortedBy { chainLink -> chainLink.name.value }

        chainLinkSearchListState.clear()
        chainLinkSearchListState.addAll(chainLinks)
    }

    fun endSearch(chainLink: ChainLink? = null) {
        chainLink?.isLatest = true

        isSearchState.value = false
        searchKeywordState.value = ""

        chainLinkSearchListState.clear()
    }

    suspend fun getAll() = chainApi.key(chain!!.id).mapCatching { chainKeyEntity ->
        val secretKey = chain!!.secretKey()
        val privateKey = chain!!.privateKey(secretKey)

        val saltKey = chain!!.saltKey(privateKey, chainKeyEntity.key)

        chainLinkApi.read(ChainKeyEntity(chain!!.id, saltKey.value)).getOrThrow()
    }.map { chainLinkEntities ->
        chainLinks = chainLinkEntities.map { chainLinkEntity ->
            ChainLink(chain!!).apply {
                id = chainLinkEntity.id
                name = ChainLink.Name(chainLinkEntity.name)
                description = ChainLink.Description(chainLinkEntity.description)
                password = ChainLink.Password(chainLinkEntity.password)
                status = ChainLink.Status.ACTUAL
            }
        }

        val chainLinksDraft = chainLinkListState
            .filter { chainLink -> chainLink.status == ChainLink.Status.DRAFT }
            .filter { chainLink -> chainLink.chain.id == chain!!.id }

        val chainLinks = chainLinks
            .map { chainLink ->
                chainLinkListState.firstOrNull { chainLinkToFind -> chainLink.id == chainLinkToFind.id }?.let {
                    if (it.status == ChainLink.Status.EDIT) {
                        ChainLink(it)
                    } else ChainLink(chainLink)
                } ?: ChainLink(chainLink)
            }
            .sortedBy { chainLink -> chainLink.name.value }
            .plus(chainLinksDraft)

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)

        Unit
    }

    suspend fun new(chainLink: ChainLink) = chainApi.key(chainLink.chain.id).mapCatching { chainKeyEntity ->
        val secretKey = chainLink.chain.secretKey()
        val privateKey = chainLink.chain.privateKey(secretKey)

        val saltKey = chainLink.chain.saltKey(privateKey, chainKeyEntity.key)

        chainLink.password = chainLink.privatePassword(secretKey)

        val chainLinkEntity = ChainLinkEntity(
            chainLink.id,
            chainLink.name.value,
            chainLink.description.value,
            chainLink.password.value,
            ChainKeyEntity(chainLink.chain.id, saltKey.value)
        )

        chainLinkApi.create(listOf(chainLinkEntity)).getOrThrow()

        chainLink.status = ChainLink.Status.ACTUAL

        update()
    }

    suspend fun edit(chainLink: ChainLink) = chainApi.key(chainLink.chain.id).mapCatching { chainKeyEntity ->
        val secretKey = chainLink.chain.secretKey()
        val privateKey = chainLink.chain.privateKey(secretKey)

        val saltKey = chainLink.chain.saltKey(privateKey, chainKeyEntity.key)

        chainLink.password = chainLink.privatePassword(secretKey)

        val chainLinkEntity = ChainLinkEntity(
            chainLink.id,
            chainLink.name.value,
            chainLink.description.value,
            chainLink.password.value,
            ChainKeyEntity(chainLink.chain.id, saltKey.value)
        )

        chainLinkApi.update(chainLinkEntity).getOrThrow()

        chainLink.status = ChainLink.Status.ACTUAL

        update()
    }

    suspend fun remove(chainLink: ChainLink) = chainApi.key(chainLink.chain.id).mapCatching { chainKeyEntity ->
        val secretKey = chainLink.chain.secretKey()
        val privateKey = chainLink.chain.privateKey(secretKey)

        val saltKey = chainLink.chain.saltKey(privateKey, chainKeyEntity.key)

        val chainLinkEntity = ChainLinkEntity(
            chainLink.id,
            chainLink.name.value,
            chainLink.description.value,
            chainLink.password.value,
            ChainKeyEntity(chainLink.chain.id, saltKey.value)
        )

        chainLinkApi.delete(chainLinkEntity).getOrThrow()

        update()
    }

    private fun update() {
        val chainLinksRemove = chainLinks.filter { chainLink ->
            !chainLinkListState.any { chainLinkToFind -> chainLink.id == chainLinkToFind.id }
        }

        chainLinks = chainLinkListState
            .filter { chainLink -> chainLink.status != ChainLink.Status.DRAFT }
            .map { chainLink ->
                if (chainLink.status == ChainLink.Status.EDIT) {
                    chainLinks.first { chainLinkToFind -> chainLink.id == chainLinkToFind.id }
                } else ChainLink(chainLink)
            }
            .plus(chainLinksRemove)

        val chainLinksDraft = chainLinkListState.filter { chainLink -> chainLink.status == ChainLink.Status.DRAFT }

        val chainLinks = chainLinkListState
            .filter { chainLink -> chainLink.status != ChainLink.Status.DRAFT }
            .sortedBy { chainLink -> chainLink.name.value }
            .plus(chainLinksDraft)

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)
    }
}