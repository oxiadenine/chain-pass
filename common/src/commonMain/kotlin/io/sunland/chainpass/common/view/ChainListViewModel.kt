package io.sunland.chainpass.common.view

import androidx.compose.runtime.*
import io.sunland.chainpass.common.*
import io.sunland.chainpass.common.network.ChainApi
import io.sunland.chainpass.common.network.WebSocket
import io.sunland.chainpass.common.repository.ChainEntity
import io.sunland.chainpass.common.repository.ChainLinkEntity
import io.sunland.chainpass.common.repository.ChainLinkRepository
import io.sunland.chainpass.common.repository.ChainRepository
import io.sunland.chainpass.common.security.PasswordGenerator
import kotlinx.coroutines.coroutineScope

class ChainListViewModel(
    val passwordGenerator: PasswordGenerator,
    private val chainRepository: ChainRepository,
    private val chainLinkRepository: ChainLinkRepository
) {
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

    fun getAll() {
        val chains = chainRepository.getAll().map { chainEntity ->
            Chain().apply {
                id = chainEntity.id
                name = Chain.Name(chainEntity.name)
            }
        }.sortedBy { chain -> chain.name.value }

        chainListState.clear()
        chainListState.addAll(chains)
    }

    suspend fun new(chainName: Chain.Name, chainKey: Chain.Key) = coroutineScope {
        val chainDraft = Chain().apply {
            name = chainName
            key = chainKey
            isDraft = true
        }

        val secretKey = chainDraft.secretKey()
        val privateKey = chainDraft.privateKey(secretKey)

        val chainEntity = ChainEntity(chainDraft.id, chainDraft.name.value, privateKey.value)

        chainDraft.key = Chain.Key()

        chainSelected = Chain(chainDraft)

        var chains = chainListState.plus(chainDraft).sortedBy { chain -> chain.name.value }

        chainListState.clear()
        chainListState.addAll(chains)

        chainRepository.create(chainEntity)

        chains = chainListState.map { chain ->
            if (chain.id == chainDraft.id) {
                chain.isDraft = false
            }

            chain
        }.sortedBy { chain -> chain.name.value }

        chainListState.clear()
        chainListState.addAll(chains)
    }

    fun select(chainKey: Chain.Key) = runCatching {
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

    fun remove() = runCatching {
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

    fun store(storageType: StorageType, storeIsPrivate: Boolean) = runCatching {
        val storableOptions = StorableOptions(storeIsPrivate, false)
        val storableChains = chainRepository.getAll().map { chainEntity ->
            val storableChainLinks = chainLinkRepository.getBy(chainEntity.id).map { chainLink ->
                StorableChainLink(chainLink.name, chainLink.description, chainLink.password)
            }

            StorableChain(chainEntity.name, chainEntity.key, storableChainLinks)
        }

        val storable = Storable(storableOptions, storableChains)

        chainRepository.store(storageType, storable)
    }

    fun unstore(filePath: FilePath) = runCatching {
        val storable = chainRepository.unstore(filePath.value)

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
        val webSocket = WebSocket.connect(deviceAddress)

        ChainApi(chainRepository, chainLinkRepository, webSocket).sync().getOrThrow()
    }
}

@Composable
fun rememberChainListViewModel(
    passwordGenerator: PasswordGenerator,
    chainRepository: ChainRepository,
    chainLinkRepository: ChainLinkRepository
) = remember { ChainListViewModel(passwordGenerator, chainRepository, chainLinkRepository) }