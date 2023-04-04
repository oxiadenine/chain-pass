package io.sunland.chainpass.common.view

import androidx.compose.runtime.*
import io.sunland.chainpass.common.*
import io.sunland.chainpass.common.network.ChainLinkApi
import io.sunland.chainpass.common.network.WebSocket
import io.sunland.chainpass.common.repository.ChainLinkEntity
import io.sunland.chainpass.common.repository.ChainLinkRepository
import io.sunland.chainpass.common.security.PasswordGenerator
import kotlinx.coroutines.coroutineScope
import java.lang.IllegalStateException

class ChainLinkListViewModel(
    val passwordGenerator: PasswordGenerator,
    private val chainLinkRepository: ChainLinkRepository,
    val chain: Chain
) {
    val chainLinkListState = mutableStateListOf<ChainLink>()

    val chainLinkSelectedIndex: Int
        get() = chainLinkListState.indexOfFirst { chainLink -> chainLink.id == chainLinkSelected?.id }

    var chainLinkEdit by mutableStateOf<ChainLink?>(null)
        private set

    var chainLinkSelected by mutableStateOf<ChainLink?>(null)

    private val chainLinkRemovedListState = mutableStateListOf<ChainLink>()

    fun startEdit(chainLink: ChainLink) {
        chainLinkEdit = ChainLink(chain).apply {
            id = chainLink.id
            name = chainLink.name
            description = chainLink.description
            password = chainLink.plainPassword(chainLink.chain.secretKey())
        }
    }

    fun cancelEdit() {
        chainLinkEdit = chainLinkListState.first { chainLink -> chainLink.id == chainLinkEdit!!.id }
    }

    fun removeLater(chainLinkRemove: ChainLink) {
        if (chainLinkListState.removeIf { chainLink -> chainLink.id == chainLinkRemove.id }) {
            chainLinkRemovedListState.add(ChainLink(chainLinkRemove))
        }
    }

    fun undoRemove() {
        chainLinkSelected = chainLinkRemovedListState.removeAt(0)

        val chainLinks = chainLinkListState.plus(chainLinkSelected!!).sortedBy { chainLink -> chainLink.name.value }

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)
    }

    fun copyPassword(chainLink: ChainLink): ChainLink.Password {
        val secretKey = chainLink.chain.secretKey()

        return chainLink.plainPassword(secretKey)
    }

    fun getAll() {
        val chainLinks = chainLinkRepository.getBy(chain.id).map { chainLinkEntity ->
            ChainLink(chain).apply {
                id = chainLinkEntity.id
                name = ChainLink.Name(chainLinkEntity.name)
                description = ChainLink.Description(chainLinkEntity.description)
                password = ChainLink.Password(chainLinkEntity.password)
            }
        }.sortedBy { chainLink -> chainLink.name.value }

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)
    }

    suspend fun new(
        chainLinkName: ChainLink.Name,
        chainLinkDescription: ChainLink.Description,
        chainLinkPassword : ChainLink.Password
    ) = coroutineScope {
        val chainLinkDraft = ChainLink(chain).apply {
            name = chainLinkName
            description = chainLinkDescription
            password = chainLinkPassword
            isDraft = true
        }

        val secretKey = chainLinkDraft.chain.secretKey()

        chainLinkDraft.password = chainLinkDraft.privatePassword(secretKey)

        val chainLinkEntity = ChainLinkEntity(
            chainLinkDraft.id,
            chainLinkDraft.name.value,
            chainLinkDraft.description.value,
            chainLinkDraft.password.value,
            chainLinkDraft.chain.id
        )

        chainLinkSelected = ChainLink(chainLinkDraft)

        var chainLinks = chainLinkListState.plus(chainLinkSelected!!).sortedBy { chainLink -> chainLink.name.value }

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)

        chainLinkRepository.create(chainLinkEntity)

        chainLinks = chainLinkListState.map { chainLink ->
            if (chainLink.id == chainLinkDraft.id) {
                chainLink.isDraft = false
            }

            chainLink
        }.sortedBy { chainLink -> chainLink.name.value }

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)
    }

    fun edit(chainLinkDescription: ChainLink.Description, chainLinkPassword: ChainLink.Password) {
        val chainLinkEdit = ChainLink(chain).apply {
            id = chainLinkEdit!!.id
            name = chainLinkEdit!!.name
            description = chainLinkDescription
            password = chainLinkPassword
        }

        val secretKey = chainLinkEdit.chain.secretKey()

        chainLinkEdit.password = chainLinkEdit.privatePassword(secretKey)

        val chainLinkEntity = ChainLinkEntity(
            chainLinkEdit.id,
            chainLinkEdit.name.value,
            chainLinkEdit.description.value,
            chainLinkEdit.password.value,
            chainLinkEdit.chain.id
        )

        chainLinkRepository.update(chainLinkEntity)

        chainLinkSelected = chainLinkEdit

        val chainLinks = chainLinkListState.map { chainLink ->
            if (chainLink.id == chainLinkEdit.id) {
                chainLinkEdit
            } else chainLink
        }.sortedBy { chainLink -> chainLink.name.value }

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)
    }

    fun remove() {
        val chainLink = chainLinkRemovedListState.first()

        val chainLinkEntity = ChainLinkEntity(
            chainLink.id,
            chainLink.name.value,
            chainLink.description.value,
            chainLink.password.value,
            chainLink.chain.id
        )

        chainLinkRepository.delete(chainLinkEntity)

        chainLinkRemovedListState.remove(chainLink)
    }

    fun store(storageType: StorageType, storeIsPrivate: Boolean) = runCatching {
        val storableOptions = StorableOptions(storeIsPrivate)
        val storableChainLinks = chainLinkRepository.getBy(chain.id).map { chainLinkEntity ->
            val chainLink = ChainLink(chain).apply {
                id = chainLinkEntity.id
                name = ChainLink.Name(chainLinkEntity.name)
                description = ChainLink.Description(chainLinkEntity.description)
                password = ChainLink.Password(chainLinkEntity.password)
            }

            StorableChainLink(
                chainLink.name.value,
                chainLink.description.value,
                if (!storableOptions.isPrivate) {
                    chainLink.plainPassword(chain.secretKey()).value
                } else chainLink.password.value
            )
        }
        val storableChains = listOf(StorableChain(
            chain.name.value, chain.privateKey(chain.secretKey()).value,
            storableChainLinks
        ))

        val storable = Storable(storableOptions, storableChains)

        chainLinkRepository.store(storageType, storable)
    }

    fun unstore(filePath: FilePath) = runCatching {
        val storable = chainLinkRepository.unstore(filePath.value)

        if (storable.chains.isEmpty()) {
            throw IllegalStateException("Invalid empty store file")
        }

        if (storable.chains.size > 1) {
            throw IllegalStateException("Invalid multiple store file")
        }

        val chains = storable.chains.map { storableChain ->
            Chain(chain).apply {
                name = Chain.Name(storableChain.name)
                key = Chain.Key(storableChain.key)
            }
        }

        chains[0].validateKey(chain.privateKey(chain.secretKey()))

        val chainLinks = mutableStateListOf<ChainLink>()

        storable.chains[0].chainLinks.forEach { storableChainLink ->
            val chainLink = ChainLink(chain).apply {
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
                    chainLink.privatePassword(chain.secretKey()).value
                } else chainLink.password.value,
                chainLink.chain.id
            )

            chainLinkRepository.create(chainLinkEntity)
        }
    }

    suspend fun sync(deviceAddress: String) = runCatching {
        val webSocket = WebSocket.connect(deviceAddress)

        ChainLinkApi(chainLinkRepository, webSocket).sync(chain.id).getOrThrow()
    }
}

@Composable
fun rememberChainLinkListViewModel(
    passwordGenerator: PasswordGenerator,
    chainLinkRepository: ChainLinkRepository,
    chain: Chain
) = remember { ChainLinkListViewModel(passwordGenerator, chainLinkRepository, chain) }