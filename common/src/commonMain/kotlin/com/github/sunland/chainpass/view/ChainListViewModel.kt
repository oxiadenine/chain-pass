package com.github.sunland.chainpass.view

import androidx.compose.runtime.*
import com.github.sunland.chainpass.*
import com.github.sunland.chainpass.network.ChainApi
import com.github.sunland.chainpass.network.WebSocket
import com.github.sunland.chainpass.repository.ChainEntity
import com.github.sunland.chainpass.repository.ChainLinkEntity
import com.github.sunland.chainpass.repository.ChainLinkRepository
import com.github.sunland.chainpass.repository.ChainRepository
import com.github.sunland.chainpass.security.PasswordEncoder

class ChainListViewModel(
    private val chainRepository: ChainRepository,
    private val chainLinkRepository: ChainLinkRepository
) {
    object StorableFormatError : Error() {
        private fun readResolve(): Any = StorableFormatError
    }

    object StorablePrivateError : Error() {
        private fun readResolve(): Any = StorablePrivateError
    }

    object SyncNetworkError : Error() {
        private fun readResolve(): Any = SyncNetworkError
    }

    val chainListState = mutableStateListOf<Chain>()

    val chainSelectedIndex: Int
        get() = chainListState.indexOfFirst { chain -> chain.id == chainSelected?.id }

    var chainSelected by mutableStateOf<Chain?>(null)
        private set

    private val chainRemovedListState = mutableStateListOf<Chain>()

    fun selectForKey(chain: Chain) {
        chainSelected = Chain(chain)
    }

    fun removeLater(chainKey: Chain.Key) {
        if (chainListState.removeIf { chain -> chain.id == chainSelected!!.id }) {
            val chain = Chain(chainSelected!!).apply { key = chainKey }

            chain.key = chain.secretKey()
            chain.key = chain.privateKey(chain.key)

            chainRemovedListState.add(chain)
        }
    }

    suspend fun getAll() {
        val chains = chainRepository.getAll().map { chainEntity ->
            Chain().apply {
                id = chainEntity.id
                name = Chain.Name(chainEntity.name)
                key = Chain.Key(chainEntity.key)
                salt = chainEntity.salt
            }
        }.sortedBy { chain -> chain.name.value }

        chainListState.clear()
        chainListState.addAll(chains)
    }

    suspend fun new(chainName: Chain.Name, chainKey: Chain.Key) {
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

        chainSelected = chainDraft

        val chains = chainListState.plus(chainDraft).sortedBy { chain -> chain.name.value }

        chainListState.clear()
        chainListState.addAll(chains)
    }

    suspend fun select(chainKey: Chain.Key) = runCatching {
        val chain = Chain(chainSelected!!).apply { key = chainKey }

        val secretKey = chain.secretKey()

        chain.key = secretKey
        chain.key = chain.privateKey(chain.key)

        val chainEntity = chainRepository.getOne(chain.id).getOrThrow()

        Chain().apply {
            id = chainEntity.id
            name = Chain.Name(chainEntity.name)
            key = Chain.Key(chainEntity.key)
            salt = chainEntity.salt
        }.validateKey(chain.key)

        chainSelected!!.key = secretKey
    }

    suspend fun undoRemove() = runCatching {
        val chainRemoved = chainRemovedListState.removeAt(0)

        val chainEntity = chainRepository.getOne(chainRemoved.id).getOrThrow()

        val chains = chainListState.plus(
            Chain().apply {
                id = chainEntity.id
                name = Chain.Name(chainEntity.name)
                key = Chain.Key(chainEntity.key)
                salt = chainEntity.salt
            }
        ).sortedBy { chain -> chain.name.value }

        chainListState.clear()
        chainListState.addAll(chains)
    }

    suspend fun remove() = runCatching {
        val chain = chainRemovedListState.first()

        val chainEntity = chainRepository.getOne(chain.id).getOrThrow()

        Chain().apply {
            id = chainEntity.id
            name = Chain.Name(chainEntity.name)
            key = Chain.Key(chainEntity.key)
            salt = chainEntity.salt
        }.validateKey(chain.key)

        chainRepository.delete(chain.id)

        chainRemovedListState.remove(chain)
    }

    suspend fun store(storageType: StorageType, storeIsPrivate: Boolean): String {
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

        return chainRepository.storage.store(storageType, storable)
    }

    suspend fun unstore(fileSelected: FileSelected) = runCatching {
        val storable = try {
            chainRepository.storage.unstore(fileSelected.path, fileSelected.bytes)
        } catch (e: IllegalStateException) {
            throw StorableFormatError
        }

        if (!storable.options.isPrivate) {
            throw StorablePrivateError
        }

        if (storable.chains.isEmpty()) {
            return@runCatching
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
    }

    suspend fun sync(deviceAddress: String) = runCatching {
        try {
            val webSocket = WebSocket.connect(deviceAddress)

            ChainApi(chainRepository, chainLinkRepository, webSocket).sync().getOrThrow()
        } catch (e: Exception) {
            throw SyncNetworkError
        }
    }
}

@Composable
fun rememberChainListViewModel(chainRepository: ChainRepository, chainLinkRepository: ChainLinkRepository) = remember {
    ChainListViewModel(chainRepository, chainLinkRepository)
}