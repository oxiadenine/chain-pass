package io.sunland.chainpass.common.view

import androidx.compose.runtime.mutableStateListOf
import io.sunland.chainpass.common.*
import io.sunland.chainpass.common.network.ChainApi
import io.sunland.chainpass.common.network.WebSocket
import io.sunland.chainpass.common.repository.ChainEntity
import io.sunland.chainpass.common.repository.ChainLinkEntity
import io.sunland.chainpass.common.repository.ChainLinkRepository
import io.sunland.chainpass.common.repository.ChainRepository
import io.sunland.chainpass.common.security.PasswordGenerator

class ChainListViewModel(
    private val chainRepository: ChainRepository,
    private val chainLinkRepository: ChainLinkRepository,
    private val passwordGenerator: PasswordGenerator,
    private val storage: Storage
) {
    val chainListState = mutableStateListOf<Chain>()

    val chainSelectedIndex: Int
        get() = chainListState.indexOfFirst { chain -> chain.id == chainSelected?.id }

    var chainSelected: Chain? = null
        private set

    private var chains = emptyList<Chain>()

    fun draft(): Chain {
        chainSelected = Chain(passwordGenerator)

        return chainSelected!!
    }

    fun selectForKey(chain: Chain) = withSelection(chain)

    fun removeLater(chainRemove: Chain) = withSelection(chainRemove) {
        chainListState.removeIf { chain -> chain.id == chainRemove.id }
    }

    fun undoRemove(chainRemove: Chain) = withSelection(chainRemove) {
        val chains = chainListState.plus(chainRemove.apply {
            key = Chain.Key()
        }).sortedBy { chain -> chain.name.value }

        chainListState.clear()
        chainListState.addAll(chains)
    }

    fun getAll() {
        chains = chainRepository.getAll().map { chainEntity ->
            Chain(passwordGenerator).apply {
                id = chainEntity.id
                name = Chain.Name(chainEntity.name)
            }
        }

        val chains = chains.map { chain -> Chain(chain) }.sortedBy { chain -> chain.name.value }

        chainListState.clear()
        chainListState.addAll(chains)
    }

    fun new(chain: Chain) = withUpdate {
        val secretKey = chain.secretKey()
        val privateKey = chain.privateKey(secretKey)

        val chainEntity = ChainEntity(chain.id, chain.name.value, privateKey.value)

        chainRepository.create(chainEntity)

        chain.key = Chain.Key()

        chainListState.add(chain)
    }

    fun select(chain: Chain) = runCatching {
        val chainEntity = chainRepository.getOne(chain.id).getOrThrow()

        val secretKey = chain.secretKey()
        val privateKey = chain.privateKey(secretKey)

        Chain(passwordGenerator).apply {
            id = chainEntity.id
            name = Chain.Name(chainEntity.name)
            key = Chain.Key(chainEntity.key)
        }.validateKey(privateKey)
    }

    fun remove(chain: Chain) = runCatching {
        withUpdate {
            val chainEntity = chainRepository.getOne(chain.id).getOrThrow()

            val secretKey = chain.secretKey()
            val privateKey = chain.privateKey(secretKey)

            Chain(passwordGenerator).apply {
                id = chainEntity.id
                name = Chain.Name(chainEntity.name)
                key = Chain.Key(chainEntity.key)
            }.validateKey(privateKey)

            chainRepository.delete(chain.id)
        }
    }

    fun store(storeOptions: StoreOptions) = runCatching {
        val chains = chainRepository.getAll().map { chainEntity ->
            Chain(passwordGenerator).apply {
                id = chainEntity.id
                name = Chain.Name(chainEntity.name)
                key = Chain.Key(chainEntity.key)
            }
        }

        val chainLinks = chains.flatMap { chain ->
            chainLinkRepository.getBy(chain.id).map { chainLinkEntity ->
                ChainLink(chain).apply {
                    id = chainLinkEntity.id
                    name = ChainLink.Name(chainLinkEntity.name)
                    description = ChainLink.Description(chainLinkEntity.description)
                    password = ChainLink.Password(chainLinkEntity.password)
                }
            }
        }

        val storableOptions = StorableOptions(storeOptions.isPrivate, storeOptions.isSingle)
        val storableChains = chains.map { chain ->
            val storableChainLinks = chainLinks
                .filter { chainLink -> chainLink.chain.id == chain.id }
                .map { chainLink ->
                    StorableChainLink(chainLink.name.value, chainLink.description.value, chainLink.password.value)
                }

            StorableChain(chain.name.value, chain.key.value, storableChainLinks)
        }

        val storable = Storable(storableOptions, storableChains)

        storage.store(storable, storeOptions.type)
    }

    fun unstore(filePath: FilePath) = runCatching {
        val storable = storage.unstore(filePath.value)

        val chains = mutableStateListOf<Chain>()
        val chainLinks = mutableStateListOf<ChainLink>()

        storable.chains.forEach { storableChain ->
            val chain = Chain(passwordGenerator).apply {
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

    private fun withSelection(chain: Chain, action: () -> Unit = {}) {
        chainSelected = chain

        action()
    }

    private fun withUpdate(action: () -> Unit) {
        action()

        val chainsRemove = chains.filter { chain ->
            !chainListState.any { chainToFind -> chain.id == chainToFind.id }
        }

        chains = chainListState.map { chain -> Chain(chain) }.plus(chainsRemove)

        val chains = chainListState.sortedBy { chain -> chain.name.value }

        chainListState.clear()
        chainListState.addAll(chains)
    }
}