package io.github.oxiadenine.chainpass.view

import androidx.compose.runtime.*
import io.github.oxiadenine.chainpass.network.ChainLinkApi
import io.github.oxiadenine.chainpass.repository.ChainLinkEntity
import io.github.oxiadenine.chainpass.repository.ChainLinkRepository
import io.github.oxiadenine.chainpass.security.PasswordEncoder
import io.github.oxiadenine.chainpass.*
import io.github.oxiadenine.chainpass.network.SyncClient
import io.github.oxiadenine.chainpass.repository.ChainRepository
import kotlin.IllegalStateException

class ChainLinkListViewModel(
    private val chainRepository: ChainRepository,
    private val chainLinkRepository: ChainLinkRepository
) {
    object StorableFormatError : Error() {
        private fun readResolve(): Any = StorableFormatError
    }

    object StorableMultipleError : Error() {
        private fun readResolve(): Any = StorableMultipleError
    }

    object StorablePrivateError : Error() {
        private fun readResolve(): Any = StorablePrivateError
    }

    object SyncNetworkError : Error() {
        private fun readResolve(): Any = SyncNetworkError
    }

    var chain = mutableStateOf<Chain?>(null)

    val chainLinkListState = mutableStateListOf<ChainLink>()

    val chainLinkSelectedIndex: Int
        get() = chainLinkListState.indexOfFirst { chainLink -> chainLink.id == chainLinkSelected?.id }

    var chainLinkEdit by mutableStateOf<ChainLink?>(null)
        private set

    private var chainLinkSelected by mutableStateOf<ChainLink?>(null)

    private val chainLinkRemovedListState = mutableStateListOf<ChainLink>()

    fun startEdit(chainLink: ChainLink) {
        chainLinkEdit = ChainLink(chainLink.chain).apply {
            id = chainLink.id
            name = chainLink.name
            description = chainLink.description
            iv = chainLink.iv
            password = chainLink.plainPassword(chainLink.chain.key)
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

        val chainLinks = chainLinkListState.plus(chainLinkSelected!!)
            .sortedBy { chainLink -> chainLink.name.value }

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)
    }

    fun copyPassword(chainLink: ChainLink): ChainLink.Password {
        return chainLink.plainPassword(chainLink.chain.key)
    }

    suspend fun getAll(chainId: String, chainKey: Chain.Key) {
        chain.value = chainRepository.getOne(chainId).getOrThrow().let { chainEntity ->
            Chain().apply {
                id = chainEntity.id
                name = Chain.Name(chainEntity.name)
                key = chainKey
                salt = chainEntity.salt
            }
        }

        val chainLinks = chainLinkRepository.getBy(chain.value!!.id).map { chainLinkEntity ->
            ChainLink(chain.value!!).apply {
                id = chainLinkEntity.id
                name = ChainLink.Name(chainLinkEntity.name)
                description = ChainLink.Description(chainLinkEntity.description)
                password = ChainLink.Password(chainLinkEntity.password)
                iv = chainLinkEntity.iv
            }
        }.sortedBy { chainLink -> chainLink.name.value }

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)
    }

    suspend fun new(
        chainLinkName: ChainLink.Name,
        chainLinkDescription: ChainLink.Description,
        chainLinkPassword : ChainLink.Password
    ) {
        val chainLinkDraft = ChainLink(chain.value!!).apply {
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

        chainLinkSelected = chainLinkDraft

        val chainLinks = chainLinkListState.plus(chainLinkDraft)
            .sortedBy { chainLink -> chainLink.name.value }

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)
    }

    suspend fun edit(chainLinkDescription: ChainLink.Description, chainLinkPassword: ChainLink.Password) {
        val chainLinkEdit = ChainLink(chain.value!!).apply {
            id = chainLinkEdit!!.id
            name = chainLinkEdit!!.name
            description = chainLinkDescription
            password = chainLinkPassword
        }

        chainLinkEdit.iv = PasswordEncoder.IV.generate()
        chainLinkEdit.password = chainLinkEdit.privatePassword(chainLinkEdit.chain.key)

        val chainLinkEntity = ChainLinkEntity(
            chainLinkEdit.id,
            chainLinkEdit.name.value,
            chainLinkEdit.description.value,
            chainLinkEdit.password.value,
            chainLinkEdit.iv,
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

    suspend fun remove() {
        val chainLink = chainLinkRemovedListState.first()

        val chainLinkEntity = ChainLinkEntity(
            chainLink.id,
            chainLink.name.value,
            chainLink.description.value,
            chainLink.password.value,
            chainLink.iv,
            chainLink.chain.id
        )

        chainLinkRepository.delete(chainLinkEntity)

        chainLinkRemovedListState.remove(chainLink)
    }

    suspend fun select(chainLinkId: String) {
        chainLinkSelected = chainLinkRepository.getOne(chainLinkId).getOrThrow().let { chainLinkEntity ->
            ChainLink(chain.value!!).apply {
                id = chainLinkEntity.id
                name = ChainLink.Name(chainLinkEntity.name)
                description = ChainLink.Description(chainLinkEntity.description)
                password = ChainLink.Password(chainLinkEntity.password)
                iv = chainLinkEntity.iv
            }
        }
    }

    suspend fun store(storageType: StorageType, storeIsPrivate: Boolean): String {
        val storableOptions = StorableOptions(storeIsPrivate)

        val storableChainLinks = chainLinkRepository.getBy(chain.value!!.id).map { chainLinkEntity ->
            val chainLink = ChainLink(chain.value!!).apply {
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
            chain.value!!.name.value,
            chain.value!!.privateKey(chain.value!!.key).value,
            chain.value!!.salt,
            storableChainLinks
        )
        )

        val storable = Storable(storableOptions, storableChains)

        return chainLinkRepository.storage.store(storageType, storable)
    }

    suspend fun unstore(fileSelected: FileSelected) = runCatching {
        val storable = try {
            chainLinkRepository.storage.unstore(fileSelected.path, fileSelected.bytes)
        } catch (e: IllegalStateException) {
            throw StorableFormatError
        }

        if (storable.options.isPrivate) {
            throw StorablePrivateError
        }

        if (storable.chains.isEmpty()) {
            return@runCatching
        }

        if (storable.chains.size > 1) {
            throw StorableMultipleError
        }

        val chainLinks = mutableStateListOf<ChainLink>()

        storable.chains[0].chainLinks.forEach { storableChainLink ->
            val chainLink = ChainLink(chain.value!!).apply {
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
    }

    suspend fun sync(deviceAddress: String) = runCatching {
        try {
            val syncClient = SyncClient(deviceAddress)

            ChainLinkApi(chainLinkRepository, syncClient).sync(chain.value!!.id)
                .onFailure { syncClient.disconnect() }.getOrThrow()
        } catch (e: Exception) {
            throw SyncNetworkError
        }
    }
}

@Composable
fun rememberChainLinkListViewModel(
    chainRepository: ChainRepository,
    chainLinkRepository: ChainLinkRepository
) = remember {
    ChainLinkListViewModel(chainRepository, chainLinkRepository)
}