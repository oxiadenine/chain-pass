package io.sunland.chainpass.common.view

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import io.sunland.chainpass.common.*
import io.sunland.chainpass.common.network.ChainLinkApi
import io.sunland.chainpass.common.network.WebSocket
import io.sunland.chainpass.common.repository.ChainLinkEntity
import io.sunland.chainpass.common.repository.ChainLinkRepository
import java.lang.IllegalStateException

class ChainLinkListViewModel(private val chainLinkRepository: ChainLinkRepository, private val storage: Storage) {
    val chainLinkListState = mutableStateListOf<ChainLink>()
    val chainLinkSearchListState = mutableStateListOf<ChainLink>()

    val isSearchState = mutableStateOf(false)
    val searchKeywordState = mutableStateOf("")

    val chainLinkSelectedIndex: Int
        get() = chainLinkListState.indexOfFirst { chainLink -> chainLink.id == chainLinkSelected?.id }

    var chain: Chain? = null

    var chainLinkSelected: ChainLink? = null
        private set

    private var chainLinks = emptyList<ChainLink>()

    fun back() {
        chainLinks = emptyList()

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)
    }

    fun draft(): ChainLink {
        chainLinkSelected = ChainLink(chain!!)

        return chainLinkSelected!!
    }

    fun startEdit(chainLinkEdit: ChainLink) = withSelection(chainLinkEdit) {
        val chainLinks = chainLinkListState.map { chainLink ->
            if (chainLink.id == chainLinkEdit.id) {
                val secretKey = chainLink.chain.secretKey()

                chainLink.password = chainLink.plainPassword(secretKey)
            }

            chainLink
        }.sortedBy { chainLink -> chainLink.name.value }

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)
    }

    fun cancelEdit(chainLinkEdit: ChainLink) = withSelection {
        val chainLinks = chainLinkListState.map { chainLink ->
            if (chainLink.id == chainLinkEdit.id) {
                val chainLinkNoEdit = chainLinks.first { chainLinkToFind -> chainLink.id == chainLinkToFind.id }

                chainLink.description = chainLinkNoEdit.description
                chainLink.password = chainLinkNoEdit.password
            }

            chainLink
        }.sortedBy { chainLink -> chainLink.name.value }

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)
    }

    fun removeLater(chainLinkRemove: ChainLink) = withSelection(chainLinkRemove) {
        chainLinkListState.removeIf { chainLink -> chainLink.id == chainLinkRemove.id }
    }

    fun undoRemove(chainLinkRemove: ChainLink) = withSelection(chainLinkRemove) {
        val chainLinks = chainLinkListState.plus(chainLinkRemove).sortedBy { chainLink -> chainLink.name.value }

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)
    }

    fun copyPassword(chainLink: ChainLink): ChainLink.Password {
        val secretKey = chainLink.chain.secretKey()

        return chainLink.plainPassword(secretKey)
    }

    fun startSearch() = withSelection {
        isSearchState.value = true
        searchKeywordState.value = ""

        val chainLinks = chainLinkListState.sortedBy { chainLink -> chainLink.name.value }

        chainLinkSearchListState.addAll(chainLinks)
    }

    fun search(keyword: String) {
        searchKeywordState.value = keyword

        val chainLinks = chainLinkListState
            .filter { chainLink -> chainLink.name.value.lowercase().contains(keyword.lowercase()) }
            .sortedBy { chainLink -> chainLink.name.value }

        chainLinkSearchListState.clear()
        chainLinkSearchListState.addAll(chainLinks)
    }

    fun endSearch(chainLink: ChainLink? = null) = withSelection(chainLink) {
        isSearchState.value = false
        searchKeywordState.value = ""

        chainLinkSearchListState.clear()
    }

    fun getAll() {
        chainLinks = chainLinkRepository.getBy(chain!!.id).map { chainLinkEntity ->
            ChainLink(chain!!).apply {
                id = chainLinkEntity.id
                name = ChainLink.Name(chainLinkEntity.name)
                description = ChainLink.Description(chainLinkEntity.description)
                password = ChainLink.Password(chainLinkEntity.password)
            }
        }

        val chainLinks = chainLinks
            .map { chainLink -> ChainLink(chainLink) }
            .sortedBy { chainLink -> chainLink.name.value }

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)
    }

    fun new(chainLink: ChainLink) = withUpdate {
        val secretKey = chainLink.chain.secretKey()

        chainLink.password = chainLink.privatePassword(secretKey)

        val chainLinkEntity = ChainLinkEntity(
            chainLink.id,
            chainLink.name.value,
            chainLink.description.value,
            chainLink.password.value,
            chainLink.chain.id
        )

        chainLinkRepository.create(chainLinkEntity)

        chainLinkListState.add(chainLink)
    }

    fun edit(chainLink: ChainLink) = withUpdate {
        val secretKey = chainLink.chain.secretKey()

        chainLink.password = chainLink.privatePassword(secretKey)

        val chainLinkEntity = ChainLinkEntity(
            chainLink.id,
            chainLink.name.value,
            chainLink.description.value,
            chainLink.password.value,
            chainLink.chain.id
        )

        chainLinkRepository.update(chainLinkEntity)
    }

    fun remove(chainLink: ChainLink) = withUpdate {
        val chainLinkEntity = ChainLinkEntity(
            chainLink.id,
            chainLink.name.value,
            chainLink.description.value,
            chainLink.password.value,
            chainLink.chain.id
        )

        chainLinkRepository.delete(chainLinkEntity)
    }

    fun store(storeOptions: StoreOptions) = runCatching {
        val chainLinks = chainLinkRepository.getBy(chain!!.id).map { chainLinkEntity ->
            ChainLink(chain!!).apply {
                id = chainLinkEntity.id
                name = ChainLink.Name(chainLinkEntity.name)
                description = ChainLink.Description(chainLinkEntity.description)
                password = ChainLink.Password(chainLinkEntity.password)
            }
        }

        val storableOptions = StorableOptions(storeOptions.isPrivate, storeOptions.isSingle)
        val storableChainLinks = chainLinks.map { chainLink ->
            StorableChainLink(
                chainLink.name.value,
                chainLink.description.value,
                if (!storeOptions.isPrivate) {
                    chainLink.plainPassword(chain!!.secretKey()).value
                } else chainLink.password.value
            )
        }
        val storableChains = listOf(
            StorableChain(chain!!.name.value, chain!!.privateKey(chain!!.secretKey()).value, storableChainLinks)
        )

        val storable = Storable(storableOptions, storableChains)

        storage.store(storable, storeOptions.type)
    }

    fun unstore(filePath: FilePath) = runCatching {
        val storable = storage.unstore(filePath.value)

        if (!storable.options.isSingle) {
            throw IllegalStateException("Invalid multiple store file ${filePath.fileName}")
        }

        val chains = storable.chains.map { storableChain ->
            Chain(chain!!.passwordGenerator).apply {
                name = Chain.Name(storableChain.name)
                key = Chain.Key(storableChain.key)
            }
        }

        chains[0].validateKey(chain!!.privateKey(chain!!.secretKey()))

        val chainLinks = mutableStateListOf<ChainLink>()

        storable.chains[0].chainLinks.forEach { storableChainLink ->
            val chainLink = ChainLink(chain!!).apply {
                name = ChainLink.Name(storableChainLink.name)
                description = ChainLink.Description(storableChainLink.description)
                password = ChainLink.Password(storableChainLink.password)
            }

            chainLinks.add(chainLink)
        }

        chainLinks.forEach { chainLink ->
            val chainLinkEntity = ChainLinkEntity(
                chainLink.id,
                chainLink.name.value,
                chainLink.description.value,
                if (!storable.options.isPrivate) {
                    chainLink.privatePassword(chain!!.secretKey()).value
                } else chainLink.password.value,
                chainLink.chain.id
            )

            chainLinkRepository.create(chainLinkEntity)
        }
    }

    suspend fun sync(deviceAddress: String) = runCatching {
        val webSocket = WebSocket.connect(deviceAddress)

        ChainLinkApi(chainLinkRepository, webSocket).sync(chain!!.id).getOrThrow()
    }

    private fun withSelection(chainLink: ChainLink? = null, action: () -> Unit = {}) {
        chainLinkSelected = chainLink

        action()
    }

    private fun withUpdate(action: () -> Unit) {
        action()

        val chainLinksRemove = chainLinks.filter { chainLink ->
            !chainLinkListState.any { chainLinkToFind -> chainLink.id == chainLinkToFind.id }
        }

        chainLinks = chainLinkListState.map { chainLink -> ChainLink(chainLink) }.plus(chainLinksRemove)

        val chainLinks = chainLinkListState.sortedBy { chainLink -> chainLink.name.value }

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)
    }
}