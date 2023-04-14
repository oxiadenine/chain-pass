package io.sunland.chainpass.common.view

import androidx.compose.runtime.*
import io.sunland.chainpass.common.*
import io.sunland.chainpass.common.network.ChainApi
import io.sunland.chainpass.common.network.WebSocket
import io.sunland.chainpass.common.repository.ChainEntity
import io.sunland.chainpass.common.repository.ChainLinkEntity
import io.sunland.chainpass.common.repository.ChainLinkRepository
import io.sunland.chainpass.common.repository.ChainRepository

class ChainListViewModel(
    private val chainRepository: ChainRepository,
    private val chainLinkRepository: ChainLinkRepository
) {
    object StorableFormatError : Error()
    object StorablePrivateError : Error()
    object SyncNetworkError : Error()

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
            chainRemovedListState.add(Chain(chainSelected!!.apply { key = chainKey }))
        }
    }

    fun undoRemove() {
        chainSelected = chainRemovedListState.removeAt(0)

        val chains = chainListState.plus(chainSelected!!.apply {
            key = Chain.Key()
        }).sortedBy { chain -> chain.name.value }

        chainListState.clear()
        chainListState.addAll(chains)
    }

    suspend fun getAll() {
        val chains = chainRepository.getAll().map { chainEntity ->
            Chain().apply {
                id = chainEntity.id
                name = Chain.Name(chainEntity.name)
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

        val secretKey = chainDraft.secretKey()
        val privateKey = chainDraft.privateKey(secretKey)

        val chainEntity = ChainEntity(chainDraft.id, chainDraft.name.value, privateKey.value)

        chainRepository.create(chainEntity)

        chainDraft.key = Chain.Key()

        chainSelected = chainDraft

        val chains = chainListState.plus(chainDraft).sortedBy { chain -> chain.name.value }

        chainListState.clear()
        chainListState.addAll(chains)
    }

    suspend fun select(chainKey: Chain.Key) = runCatching {
        val chain = chainSelected!!.apply { key = chainKey }

        val chainEntity = chainRepository.getOne(chain.id).getOrThrow()

        val secretKey = chain.secretKey()
        val privateKey = chain.privateKey(secretKey)

        Chain().apply {
            id = chainEntity.id
            name = Chain.Name(chainEntity.name)
            key = Chain.Key(chainEntity.key)
        }.validateKey(privateKey)
    }

    suspend fun remove() = runCatching {
        val chain = chainRemovedListState.first()

        val chainEntity = chainRepository.getOne(chain.id).getOrThrow()

        val secretKey = chain.secretKey()
        val privateKey = chain.privateKey(secretKey)

        Chain().apply {
            id = chainEntity.id
            name = Chain.Name(chainEntity.name)
            key = Chain.Key(chainEntity.key)
        }.validateKey(privateKey)

        chainRepository.delete(chain.id)

        chainRemovedListState.remove(chain)
    }

    suspend fun store(storageType: StorageType, storeIsPrivate: Boolean): String {
        val storableOptions = StorableOptions(storeIsPrivate)
        val storableChains = chainRepository.getAll().map { chainEntity ->
            val storableChainLinks = chainLinkRepository.getBy(chainEntity.id).map { chainLink ->
                StorableChainLink(chainLink.name, chainLink.description, chainLink.password)
            }

            StorableChain(chainEntity.name, chainEntity.key, storableChainLinks)
        }

        val storable = Storable(storableOptions, storableChains)

        return chainRepository.store(storageType, storable)
    }

    suspend fun unstore(filePath: FilePath) = runCatching {
        val storable = try {
            chainRepository.unstore(filePath.value)
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
            }

            chains.add(chain)

            chainLinks.addAll(storableChain.chainLinks.map { storableChainLink ->
                ChainLink(chain).apply {
                    name = ChainLink.Name(storableChainLink.name)
                    description = ChainLink.Description(storableChainLink.description)
                    password = ChainLink.Password(storableChainLink.password)
                }
            })
        }

        chains.forEach { chain ->
            val chainEntity = ChainEntity(chain.id, chain.name.value, chain.key.value)

            chainRepository.create(chainEntity)
        }

        chainLinks.forEach { chainLink ->
            val chainLinkEntity = ChainLinkEntity(
                chainLink.id,
                chainLink.name.value,
                chainLink.description.value,
                chainLink.password.value,
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