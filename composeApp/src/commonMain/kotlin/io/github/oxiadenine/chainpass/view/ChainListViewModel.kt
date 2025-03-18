package io.github.oxiadenine.chainpass.view

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.oxiadenine.chainpass.network.ChainApi
import io.github.oxiadenine.chainpass.repository.ChainEntity
import io.github.oxiadenine.chainpass.repository.ChainLinkEntity
import io.github.oxiadenine.chainpass.repository.ChainLinkRepository
import io.github.oxiadenine.chainpass.repository.ChainRepository
import io.github.oxiadenine.chainpass.security.PasswordEncoder
import io.github.oxiadenine.chainpass.*
import io.github.oxiadenine.chainpass.network.SyncClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChainListViewModel(
    private val chainRepository: ChainRepository,
    private val chainLinkRepository: ChainLinkRepository
) : ViewModel() {
    private val _dataStateFlow = MutableStateFlow(ChainListState())
    val dataStateFlow = _dataStateFlow.asStateFlow()

    fun getAll() {
        viewModelScope.launch(Dispatchers.IO) {
            _dataStateFlow.update { state -> state.copy(isLoading = true) }

            val chains = chainRepository.getAll().map { chainEntity ->
                Chain().apply {
                    id = chainEntity.id
                    name = Chain.Name(chainEntity.name)
                    key = Chain.Key(chainEntity.key)
                    salt = chainEntity.salt
                }
            }.sortedBy { chain -> chain.name.value }

            _dataStateFlow.update { state ->
                state.copy(isLoading = false, chains = chains)
            }
        }
    }

    fun new(chainName: Chain.Name, chainKey: Chain.Key) {
        viewModelScope.launch(Dispatchers.IO) {
            _dataStateFlow.update { state -> state.copy(isLoading = true) }

            val chainDraft = Chain().apply {
                name = chainName
                key = chainKey
            }

            chainDraft.salt = PasswordEncoder.Salt.generate()
            chainDraft.key = chainDraft.secretKey()
            chainDraft.key = chainDraft.privateKey(chainDraft.key)

            val chainEntity = ChainEntity(
                chainDraft.id,
                chainDraft.name.value,
                chainDraft.key.value,
                chainDraft.salt
            )

            chainRepository.create(chainEntity)

            _dataStateFlow.update { state ->
                state.copy(
                    isLoading = false,
                    chainSelected = chainDraft,
                    chains = state.chains.plus(chainDraft).sortedBy { chain -> chain.name.value },
                    event = ChainListEvent.ItemNew
                )
            }
        }
    }

    fun select(chain: Chain) {
        _dataStateFlow.update { state -> state.copy(chainSelected = chain) }
    }

    fun open(chainKey: Chain.Key) {
        viewModelScope.launch(Dispatchers.IO) {
            _dataStateFlow.update { state -> state.copy(isLoading = true) }

            val chainSelected = Chain(_dataStateFlow.value.chainSelected!!)

            val chain = _dataStateFlow.value.chains.first { chain ->
                chain.id == chainSelected.id
            }.apply { key = chainKey }

            val secretKey = chain.secretKey()

            chain.key = secretKey
            chain.key = chain.privateKey(chain.key)

            val chainEntity = chainRepository.getOne(chain.id).getOrThrow()

            try {
                Chain().apply {
                    id = chainEntity.id
                    name = Chain.Name(chainEntity.name)
                    key = Chain.Key(chainEntity.key)
                    salt = chainEntity.salt
                }.validateKey(chain.key)
            } catch (e: Throwable) {
                _dataStateFlow.update { state -> state.copy(isLoading = false, error = e) }

                return@launch
            }

            chain.key = secretKey

            _dataStateFlow.update { state ->
                state.copy(isLoading = false, event = ChainListEvent.ItemOpen(chain))
            }
        }
    }

    fun removeLater(chainKey: Chain.Key) {
        viewModelScope.launch(Dispatchers.IO) {
            _dataStateFlow.update { state -> state.copy(isLoading = true) }

            val chainSelected = Chain(_dataStateFlow.value.chainSelected!!)

            _dataStateFlow.value.chains.firstOrNull { chain -> chain.id == chainSelected.id }?.let { chain ->
                val chainRemoved = chain.apply { key = chainKey }

                chainRemoved.key = chainRemoved.secretKey()
                chainRemoved.key = chainRemoved.privateKey(chain.key)

                _dataStateFlow.update { state ->
                    state.copy(
                        isLoading = false,
                        chains = state.chains.filter { chain ->
                            chain.id != chainRemoved.id
                        }.sortedBy { chain -> chain.name.value },
                        chainsRemoved = state.chainsRemoved.plus(chainRemoved),
                        event = ChainListEvent.ItemRemoveLater(chainRemoved)
                    )
                }
            }
        }
    }

    fun remove() {
        viewModelScope.launch(Dispatchers.IO) {
            val chainRemoved = _dataStateFlow.value.chainsRemoved.first()

            val chainEntity = chainRepository.getOne(chainRemoved.id).getOrThrow()

            try {
                Chain().apply {
                    id = chainEntity.id
                    name = Chain.Name(chainEntity.name)
                    key = Chain.Key(chainEntity.key)
                    salt = chainEntity.salt
                }.validateKey(chainRemoved.key)
            } catch (e: Throwable) {
                _dataStateFlow.update { state ->
                    state.copy(event = ChainListEvent.ItemRemove, error = e)
                }

                return@launch
            }

            chainRepository.delete(chainRemoved.id)

            _dataStateFlow.update { state ->
                state.copy(chainsRemoved = state.chainsRemoved.drop(1))
            }
        }
    }

    fun undoRemove() {
        viewModelScope.launch(Dispatchers.IO) {
            val chainRemoved = _dataStateFlow.value.chainsRemoved.first()

            val chainEntity = chainRepository.getOne(chainRemoved.id).getOrThrow()

            val chain = Chain().apply {
                id = chainEntity.id
                name = Chain.Name(chainEntity.name)
                key = Chain.Key(chainEntity.key)
                salt = chainEntity.salt
            }

            _dataStateFlow.update { state ->
                state.copy(
                    chainSelected = chainRemoved,
                    chains = state.chains.plus(chain).sortedBy { chain -> chain.name.value },
                    chainsRemoved = state.chainsRemoved.drop(1),
                    event = ChainListEvent.ItemUndoRemove
                )
            }
        }
    }

    fun store(storageType: StorageType, storeIsPrivate: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            _dataStateFlow.update { state -> state.copy(isLoading = true) }

            val storableOptions = StorableOptions(storeIsPrivate)
            val storableChains = chainRepository.getAll().map { chainEntity ->
                val storableChainLinks = chainLinkRepository.getBy(chainEntity.id).map { chainLinkEntity ->
                    StorableChainLink(
                        chainLinkEntity.name,
                        chainLinkEntity.description,
                        chainLinkEntity.password,
                        chainLinkEntity.iv
                    )
                }

                StorableChain(chainEntity.name, chainEntity.key, chainEntity.salt, storableChainLinks)
            }

            val storable = Storable(storableOptions, storableChains)

            val fileName = chainRepository.storage.store(storageType, storable)

            _dataStateFlow.update { state ->
                state.copy(isLoading = false, event = ChainListEvent.Store(fileName))
            }
        }
    }

    fun unstore(fileSelected: FileSelected) {
        viewModelScope.launch(Dispatchers.IO) {
            _dataStateFlow.update { state -> state.copy(isLoading = true) }

            val storable = try {
                chainRepository.storage.unstore(fileSelected.path, fileSelected.bytes)
            } catch (e: Throwable) {
                _dataStateFlow.update { state -> state.copy(isLoading = false, error = e) }

                return@launch
            }

            if (!storable.options.isPrivate) {
                _dataStateFlow.update { state ->
                    state.copy(isLoading = false, error = Storage.StorablePrivateError)
                }

                return@launch
            }

            if (storable.chains.isEmpty()) {
                _dataStateFlow.update { state ->
                    state.copy(isLoading = false, event = ChainListEvent.Unstore(fileSelected.fileName))
                }

                return@launch
            }

            val chains = mutableStateListOf<Chain>()
            val chainLinks = mutableStateListOf<ChainLink>()

            storable.chains.forEach { storableChain ->
                val chain = Chain().apply {
                    name = Chain.Name(storableChain.name)
                    key = Chain.Key(storableChain.key)
                    salt = storableChain.salt
                }

                chains.add(chain)

                chainLinks.addAll(storableChain.chainLinks.map { storableChainLink ->
                    ChainLink(chain).apply {
                        name = ChainLink.Name(storableChainLink.name)
                        description = ChainLink.Description(storableChainLink.description)
                        password = ChainLink.Password(storableChainLink.password)
                        iv = storableChainLink.iv
                    }
                })
            }

            chains.forEach { chain ->
                val chainEntity = ChainEntity(chain.id, chain.name.value, chain.key.value, chain.salt)

                chainRepository.create(chainEntity)
            }

            chainLinks.forEach { chainLink ->
                val chainLinkEntity = ChainLinkEntity(
                    chainLink.id,
                    chainLink.name.value,
                    chainLink.description.value,
                    chainLink.password.value,
                    chainLink.iv,
                    chainLink.chain.id
                )

                chainLinkRepository.create(chainLinkEntity)
            }

            _dataStateFlow.update { state ->
                state.copy(isLoading = false, event = ChainListEvent.Unstore(fileSelected.fileName))
            }
        }
    }

    fun sync(deviceAddress: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _dataStateFlow.update { state -> state.copy(isLoading = true) }

            val syncClient = SyncClient(deviceAddress)

            try {
                ChainApi(chainRepository, chainLinkRepository, syncClient).sync()
                    .onFailure { syncClient.disconnect() }.getOrThrow()
            } catch (e: Throwable) {
                _dataStateFlow.update { state -> state.copy(isLoading = false, error = e) }

                return@launch
            }

            _dataStateFlow.update { state -> state.copy(isLoading = false, event = ChainListEvent.Sync) }
        }
    }

    fun clearEvent() = _dataStateFlow.update { state -> state.copy(event = null) }
    fun clearError() = _dataStateFlow.update { state -> state.copy(error = null) }
}