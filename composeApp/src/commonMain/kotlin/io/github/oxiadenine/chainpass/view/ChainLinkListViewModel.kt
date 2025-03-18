package io.github.oxiadenine.chainpass.view

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.oxiadenine.chainpass.network.ChainLinkApi
import io.github.oxiadenine.chainpass.repository.ChainLinkEntity
import io.github.oxiadenine.chainpass.repository.ChainLinkRepository
import io.github.oxiadenine.chainpass.security.PasswordEncoder
import io.github.oxiadenine.chainpass.*
import io.github.oxiadenine.chainpass.network.SyncClient
import io.github.oxiadenine.chainpass.repository.ChainRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChainLinkListViewModel(
    private val chainRepository: ChainRepository,
    private val chainLinkRepository: ChainLinkRepository
) : ViewModel() {
    private val _dataStateFlow = MutableStateFlow(ChainLinkListState())

    val dataStateFlow = _dataStateFlow.asStateFlow()

    fun getAll(chainId: String, chainKey: Chain.Key) {
        viewModelScope.launch(Dispatchers.IO) {
            _dataStateFlow.update { state -> state.copy(isLoading = true) }

            val chain = chainRepository.getOne(chainId).getOrThrow().let { chainEntity ->
                Chain().apply {
                    id = chainEntity.id
                    name = Chain.Name(chainEntity.name)
                    key = chainKey
                    salt = chainEntity.salt
                }
            }

            val chainLinks = chainLinkRepository.getBy(chain.id).map { chainLinkEntity ->
                ChainLink(chain).apply {
                    id = chainLinkEntity.id
                    name = ChainLink.Name(chainLinkEntity.name)
                    description = ChainLink.Description(chainLinkEntity.description)
                    password = ChainLink.Password(chainLinkEntity.password)
                    iv = chainLinkEntity.iv
                }
            }.sortedBy { chainLink -> chainLink.name.value }

            _dataStateFlow.update { state ->
                state.copy(isLoading = false, chain = chain, chainLinks = chainLinks)
            }
        }
    }

    fun new(
        chainLinkName: ChainLink.Name,
        chainLinkDescription: ChainLink.Description,
        chainLinkPassword : ChainLink.Password
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _dataStateFlow.update { state -> state.copy(isLoading = true) }

            val chain = _dataStateFlow.value.chain!!

            val chainLinkDraft = ChainLink(chain).apply {
                name = chainLinkName
                description = chainLinkDescription
                password = chainLinkPassword
            }

            chainLinkDraft.iv = PasswordEncoder.IV.generate()
            chainLinkDraft.password = chainLinkDraft.privatePassword(chainLinkDraft.chain.key)

            val chainLinkEntity = ChainLinkEntity(
                chainLinkDraft.id,
                chainLinkDraft.name.value,
                chainLinkDraft.description.value,
                chainLinkDraft.password.value,
                chainLinkDraft.iv,
                chainLinkDraft.chain.id
            )

            chainLinkRepository.create(chainLinkEntity)

            _dataStateFlow.update { state ->
                state.copy(
                    isLoading = false,
                    chainLinkSelected = chainLinkDraft,
                    chainLinks = state.chainLinks.plus(chainLinkDraft).sortedBy { chainLink -> chainLink.name.value }
                )
            }
        }
    }

    fun select(chainLink: ChainLink) {
        _dataStateFlow.update { state -> state.copy(chainLinkSelected = chainLink) }
    }

    fun copyPassword() {
        val chainLinkSelected = ChainLink(_dataStateFlow.value.chainLinkSelected!!)

        _dataStateFlow.update { state ->
            state.copy(event = ChainLinkListEvent.ItemCopyPassword(
                chainLinkSelected.plainPassword(chainLinkSelected.chain.key)
            ))
        }
    }

    fun startEdit() {
        val chainLinkSelected = ChainLink(_dataStateFlow.value.chainLinkSelected!!)

        val chainLinkEdit = ChainLink(chainLinkSelected.chain).apply {
            id = chainLinkSelected.id
            name = chainLinkSelected.name
            description = chainLinkSelected.description
            iv = chainLinkSelected.iv
            password = chainLinkSelected.plainPassword(chainLinkSelected.chain.key)
        }

        _dataStateFlow.update { state ->
            state.copy(chainLinkEdit = chainLinkEdit, event = ChainLinkListEvent.ItemStartEdit)
        }
    }

    fun edit(chainLinkDescription: ChainLink.Description, chainLinkPassword: ChainLink.Password) {
        viewModelScope.launch(Dispatchers.IO) {
            _dataStateFlow.update { state -> state.copy(isLoading = true) }

            val chain = _dataStateFlow.value.chain!!
            val chainLinkEdit = _dataStateFlow.value.chainLinkEdit!!

            val chainLinkEdited = ChainLink(chain).apply {
                id = chainLinkEdit.id
                name = chainLinkEdit.name
                description = chainLinkDescription
                password = chainLinkPassword
            }

            chainLinkEdited.iv = PasswordEncoder.IV.generate()
            chainLinkEdited.password = chainLinkEdited.privatePassword(chainLinkEdited.chain.key)

            val chainLinkEntity = ChainLinkEntity(
                chainLinkEdited.id,
                chainLinkEdited.name.value,
                chainLinkEdited.description.value,
                chainLinkEdited.password.value,
                chainLinkEdited.iv,
                chainLinkEdited.chain.id
            )

            chainLinkRepository.update(chainLinkEntity)

            _dataStateFlow.update { state ->
                state.copy(
                    isLoading = false,
                    chainLinkSelected = chainLinkEdited,
                    chainLinkEdit = null,
                    chainLinks = state.chainLinks.map { chainLink ->
                        if (chainLink.id == chainLinkEdited.id) {
                            chainLinkEdited
                        } else chainLink
                    }.sortedBy { chainLink -> chainLink.name.value },
                    event = ChainLinkListEvent.ItemEdit
                )
            }
        }
    }

    fun cancelEdit() {
        _dataStateFlow.update { state ->
            state.copy(chainLinkEdit = null, event = ChainLinkListEvent.ItemCancelEdit)
        }
    }

    fun removeLater() {
        val chainLinkSelected = ChainLink(_dataStateFlow.value.chainLinkSelected!!)

        _dataStateFlow.value.chainLinks.firstOrNull { chainLink ->
            chainLink.id == chainLinkSelected.id
        }?.let { chainLinkRemoved ->
            _dataStateFlow.update { state ->
                state.copy(
                    chainLinks = state.chainLinks.filter { chainLink ->
                        chainLink.id != chainLinkRemoved.id
                    }.sortedBy { chainLink -> chainLink.name.value },
                    chainLinksRemoved = state.chainLinksRemoved.plus(chainLinkRemoved),
                    event = ChainLinkListEvent.ItemRemoveLater(chainLinkRemoved)
                )
            }
        }
    }

    fun remove() {
        viewModelScope.launch(Dispatchers.IO) {
            val chainLinkRemoved = _dataStateFlow.value.chainLinksRemoved.first()

            val chainLinkEntity = ChainLinkEntity(
                chainLinkRemoved.id,
                chainLinkRemoved.name.value,
                chainLinkRemoved.description.value,
                chainLinkRemoved.password.value,
                chainLinkRemoved.iv,
                chainLinkRemoved.chain.id
            )

            chainLinkRepository.delete(chainLinkEntity)

            _dataStateFlow.update { state ->
                state.copy(
                    chainLinksRemoved = state.chainLinksRemoved.drop(1),
                    event = ChainLinkListEvent.ItemRemove
                )
            }
        }
    }

    fun undoRemove() {
        val chainLinkRemoved = _dataStateFlow.value.chainLinksRemoved.first()

        _dataStateFlow.update { state ->
            state.copy(
                chainLinkSelected = chainLinkRemoved,
                chainLinks = state.chainLinks.plus(chainLinkRemoved).sortedBy { chainLink -> chainLink.name.value },
                chainLinksRemoved = state.chainLinksRemoved.drop(1),
                event = ChainLinkListEvent.ItemUndoRemove
            )
        }
    }

    fun startSearch() {
        _dataStateFlow.update { state ->
            state.copy(isSearch = true, chainLinksSearch = state.chainLinks)
        }
    }

    fun search(keyword: String) {
        _dataStateFlow.update { state ->
            state.copy(chainLinksSearch = state.chainLinks.filter { chainLink ->
                chainLink.name.value.lowercase().contains(keyword.lowercase())
            }.sortedBy { chainLink -> chainLink.name.value })
        }
    }

    fun cancelSearch(chainLinkSearched: ChainLink? = null) {
        _dataStateFlow.update { state -> state.copy(
            isSearch = false,
            chainLinkSelected = chainLinkSearched,
            chainLinksSearch = emptyList())
        }
    }

    fun store(storageType: StorageType, storeIsPrivate: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            _dataStateFlow.update { state -> state.copy(isLoading = true) }

            val storableOptions = StorableOptions(storeIsPrivate)

            val chain = _dataStateFlow.value.chain!!

            val storableChainLinks = chainLinkRepository.getBy(chain.id).map { chainLinkEntity ->
                val chainLink = ChainLink(chain).apply {
                    id = chainLinkEntity.id
                    name = ChainLink.Name(chainLinkEntity.name)
                    description = ChainLink.Description(chainLinkEntity.description)
                    password = ChainLink.Password(chainLinkEntity.password)
                    iv = chainLinkEntity.iv
                }

                StorableChainLink(
                    chainLink.name.value,
                    chainLink.description.value,
                    if (!storableOptions.isPrivate) {
                        chainLink.plainPassword(chainLink.chain.key).value
                    } else chainLink.password.value,
                    chainLink.iv
                )
            }

            val storableChains = listOf(
                StorableChain(
                    chain.name.value,
                    chain.privateKey(chain.key).value,
                    chain.salt,
                    storableChainLinks
                )
            )

            val storable = Storable(storableOptions, storableChains)

            val fileName = chainLinkRepository.storage.store(storageType, storable)

            _dataStateFlow.update { state ->
                state.copy(isLoading = false, event = ChainLinkListEvent.Store(fileName))
            }
        }
    }

    fun unstore(fileSelected: FileSelected) {
        viewModelScope.launch(Dispatchers.IO) {
            _dataStateFlow.update { state -> state.copy(isLoading = true) }

            val storable = try {
                chainLinkRepository.storage.unstore(fileSelected.path, fileSelected.bytes)
            } catch (e: Throwable) {
                _dataStateFlow.update { state -> state.copy(isLoading = false, error = e) }

                return@launch
            }

            if (storable.options.isPrivate) {
                _dataStateFlow.update { state -> state.copy(isLoading = false, error = Storage.StorablePrivateError) }

                return@launch
            }

            if (storable.chains.isEmpty()) {
                _dataStateFlow.update { state ->
                    state.copy(isLoading = false, event = ChainLinkListEvent.Unstore(fileSelected.fileName))
                }

                return@launch
            }

            if (storable.chains.size > 1) {
                _dataStateFlow.update { state ->
                    state.copy(isLoading = false, error = Storage.StorableMultipleError)
                }

                return@launch
            }

            val chainLinks = mutableStateListOf<ChainLink>()

            val chain = _dataStateFlow.value.chain!!

            storable.chains[0].chainLinks.forEach { storableChainLink ->
                val chainLink = ChainLink(chain).apply {
                    name = ChainLink.Name(storableChainLink.name)
                    description = ChainLink.Description(storableChainLink.description)
                    password = ChainLink.Password(storableChainLink.password)
                    iv = storableChainLink.iv
                }

                chainLinks.add(chainLink)
            }

            chainLinks.forEach { chainLink ->
                val chainLinkEntity = ChainLinkEntity(
                    chainLink.id,
                    chainLink.name.value,
                    chainLink.description.value,
                    if (!storable.options.isPrivate) {
                        chainLink.privatePassword(chainLink.chain.key).value
                    } else chainLink.password.value,
                    chainLink.iv,
                    chainLink.chain.id
                )

                chainLinkRepository.create(chainLinkEntity)
            }

            _dataStateFlow.update { state ->
                state.copy(isLoading = false, event = ChainLinkListEvent.Unstore(fileSelected.fileName))
            }
        }
    }

    fun sync(deviceAddress: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _dataStateFlow.update { state -> state.copy(isLoading = true) }

            val syncClient = SyncClient(deviceAddress)

            val chain = _dataStateFlow.value.chain!!

            try {
                ChainLinkApi(chainLinkRepository, syncClient).sync(chain.id)
                    .onFailure { syncClient.disconnect() }.getOrThrow()
            } catch (e: Throwable) {
                _dataStateFlow.update { state -> state.copy(isLoading = false, error = e) }

                return@launch
            }

            _dataStateFlow.update { state ->
                state.copy(isLoading = false, event = ChainLinkListEvent.Sync)
            }
        }
    }

    fun clearEvent() = _dataStateFlow.update { state -> state.copy(event = null) }
    fun clearError() = _dataStateFlow.update { state -> state.copy(error = null) }
}