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

    var chainLinkEdited: ChainLink? = null
        private set

    private var chainLinkSelected: ChainLink? = null

    private val chainLinksRemoved = mutableListOf<ChainLink>()

    fun back() {
        chainLinkListState.clear()
    }

    fun draft() = ChainLink(chain!!)

    fun startEdit(chainLink: ChainLink) {
        chainLinkEdited = ChainLink(chain!!).apply {
            id = chainLink.id
            name = chainLink.name
            description = chainLink.description
            password = chainLink.plainPassword(chainLink.chain.secretKey())
        }
    }

    fun cancelEdit() {
        chainLinkEdited = chainLinkListState.first { chainLink -> chainLink.id == chainLinkEdited!!.id }
    }

    fun removeLater(chainLinkRemove: ChainLink) {
        if (chainLinkListState.removeIf { chainLink -> chainLink.id == chainLinkRemove.id }) {
            chainLinksRemoved.add(ChainLink(chainLinkRemove))
        }
    }

    fun undoRemove() {
        chainLinkSelected = chainLinksRemoved.removeAt(0)

        val chainLinks = chainLinkListState.plus(chainLinkSelected!!).sortedBy { chainLink -> chainLink.name.value }

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)
    }

    fun copyPassword(chainLink: ChainLink): ChainLink.Password {
        val secretKey = chainLink.chain.secretKey()

        return chainLink.plainPassword(secretKey)
    }

    fun startSearch() {
        chainLinkSelected = null

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

    fun endSearch(chainLink: ChainLink? = null) {
        chainLinkSelected = chainLink

        isSearchState.value = false
        searchKeywordState.value = ""

        chainLinkSearchListState.clear()
    }

    fun getAll() {
        val chainLinks = chainLinkRepository.getBy(chain!!.id).map { chainLinkEntity ->
            ChainLink(chain!!).apply {
                id = chainLinkEntity.id
                name = ChainLink.Name(chainLinkEntity.name)
                description = ChainLink.Description(chainLinkEntity.description)
                password = ChainLink.Password(chainLinkEntity.password)
            }
        }.sortedBy { chainLink -> chainLink.name.value }

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)
    }

    fun new(chainLinkDraft: ChainLink) {
        val secretKey = chainLinkDraft.chain.secretKey()

        chainLinkDraft.password = chainLinkDraft.privatePassword(secretKey)

        val chainLinkEntity = ChainLinkEntity(
            chainLinkDraft.id,
            chainLinkDraft.name.value,
            chainLinkDraft.description.value,
            chainLinkDraft.password.value,
            chainLinkDraft.chain.id
        )

        chainLinkRepository.create(chainLinkEntity)

        chainLinkSelected = ChainLink(chainLinkDraft)

        val chainLinks = chainLinkListState.plus(chainLinkSelected!!).sortedBy { chainLink -> chainLink.name.value }

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)
    }

    fun edit() {
        val secretKey = chainLinkEdited!!.chain.secretKey()

        chainLinkEdited!!.password = chainLinkEdited!!.privatePassword(secretKey)

        val chainLinkEntity = ChainLinkEntity(
            chainLinkEdited!!.id,
            chainLinkEdited!!.name.value,
            chainLinkEdited!!.description.value,
            chainLinkEdited!!.password.value,
            chainLinkEdited!!.chain.id
        )

        chainLinkRepository.update(chainLinkEntity)

        chainLinkSelected = ChainLink(chainLinkEdited!!)

        val chainLinks = chainLinkListState.map { chainLink ->
            if (chainLink.id == chainLinkEdited!!.id) {
                chainLinkEdited!!
            } else chainLink
        }.sortedBy { chainLink -> chainLink.name.value }

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)
    }

    fun remove() {
        val chainLink = chainLinksRemoved.first()

        val chainLinkEntity = ChainLinkEntity(
            chainLink.id,
            chainLink.name.value,
            chainLink.description.value,
            chainLink.password.value,
            chainLink.chain.id
        )

        chainLinkRepository.delete(chainLinkEntity)

        chainLinksRemoved.remove(chainLink)
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
}