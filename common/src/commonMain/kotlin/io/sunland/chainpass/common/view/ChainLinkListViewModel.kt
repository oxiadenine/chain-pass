package io.sunland.chainpass.common.view

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import io.sunland.chainpass.common.Chain
import io.sunland.chainpass.common.ChainLink
import io.sunland.chainpass.common.repository.ChainKeyEntity
import io.sunland.chainpass.common.repository.ChainLinkEntity
import io.sunland.chainpass.common.repository.ChainLinkRepository
import io.sunland.chainpass.common.repository.ChainRepository
import io.sunland.chainpass.common.security.EncoderSpec
import io.sunland.chainpass.common.security.PasswordEncoder

class ChainLinkListViewModel(
    private val chainRepository: ChainRepository,
    private val chainLinkRepository: ChainLinkRepository
) {
    var chain: Chain? = null

    val chainLinkListState = mutableStateListOf<ChainLink>()
    val chainLinkSearchListState = mutableStateListOf<ChainLink>()

    val isSearchState = mutableStateOf(false)
    val searchKeywordState = mutableStateOf("")

    private var chainLinks = emptyList<ChainLink>()

    fun draft() {
        val chainLinks = chainLinks.plus(chainLinkListState.filter { chainLink ->
            chainLink.status == ChainLink.Status.DRAFT
        })

        val chainLink = ChainLink().apply {
            id = if (chainLinks.isNotEmpty()) {
                chainLinks.maxOf { chainLink -> chainLink.id } + 1
            } else 1
        }

        chainLinkListState.add(chainLink)

        update()
    }

    fun rejectDraft(chainLink: ChainLink) {
        chainLinkListState.remove(chainLink)

        update()
    }

    fun rejectDrafts() {
        val chainLinks = chainLinkListState.filter { chainLink -> chainLink.status != ChainLink.Status.DRAFT }

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)
    }

    fun startEdit(editChainLink: ChainLink) {
        if (editChainLink.password.isPrivate) {
            unlockPassword(editChainLink)
        }

        editChainLink.status = ChainLink.Status.EDIT

        val draftChainLinks = chainLinkListState.filter { chainLink -> chainLink.status == ChainLink.Status.DRAFT }

        val chainLinks = chainLinkListState
            .filter { chainLink -> chainLink.status != ChainLink.Status.DRAFT }
            .sortedBy { chainLink -> chainLink.name.value }
            .plus(draftChainLinks)

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)
    }

    fun cancelEdit() {
        val draftChainLinks = chainLinkListState.filter { chainLink -> chainLink.status == ChainLink.Status.DRAFT }

        val chainLinks = chainLinkListState
            .filter { chainLink -> chainLink.status != ChainLink.Status.DRAFT }
            .map { chainLink ->
                if (chainLink.status == ChainLink.Status.EDIT) {
                    lockPassword(chainLink)

                    chainLink.status = ChainLink.Status.ACTUAL
                }

                chainLink
            }.sortedBy { chainLink -> chainLink.name.value }.plus(draftChainLinks)

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)
    }

    fun removeLater(chainLink: ChainLink) {
        if (!chainLink.password.isPrivate) {
            lockPassword(chainLink)
        }

        chainLinkListState.remove(chainLink)
    }

    fun undoRemove(chainLink: ChainLink) {
        chainLinkListState.add(chainLink)

        update()
    }

    fun unlockPassword(chainLink: ChainLink) {
        val secretKey = PasswordEncoder.hash(EncoderSpec.Passphrase(
            PasswordEncoder.Base64.encode(chain!!.key.value.encodeToByteArray()),
            PasswordEncoder.Base64.encode(chain!!.name.value.encodeToByteArray())
        ))

        val password = PasswordEncoder.decrypt(
            chainLink.password.value,
            EncoderSpec.Passphrase(secretKey, PasswordEncoder.Base64.encode(chainLink.name.value.encodeToByteArray()))
        )

        chainLink.password = ChainLink.Password(PasswordEncoder.Base64.decode(password).decodeToString(), false)
    }

    fun lockPassword(chainLink: ChainLink) {
        val secretKey = PasswordEncoder.hash(EncoderSpec.Passphrase(
            PasswordEncoder.Base64.encode(chain!!.key.value.encodeToByteArray()),
            PasswordEncoder.Base64.encode(chain!!.name.value.encodeToByteArray())
        ))

        val privatePassword = PasswordEncoder.encrypt(
            PasswordEncoder.Base64.encode(chainLink.password.value.encodeToByteArray()),
            EncoderSpec.Passphrase(secretKey, PasswordEncoder.Base64.encode(chainLink.name.value.encodeToByteArray()))
        )

        chainLink.password = ChainLink.Password(privatePassword)
    }

    fun startSearch() {
        isSearchState.value = true
        searchKeywordState.value = ""

        val chainLinks = chainLinkListState
            .filter { chainLink -> chainLink.status == ChainLink.Status.ACTUAL }
            .sortedBy { chainLink -> chainLink.name.value }

        println(chainLinks.size)

        chainLinkSearchListState.addAll(chainLinks)
    }

    fun search(keyword: String) {
        searchKeywordState.value = keyword

        val chainLinks = chainLinkListState
            .filter { chainLink -> chainLink.status == ChainLink.Status.ACTUAL }
            .filter { chainLink -> chainLink.name.value.lowercase().contains(keyword.lowercase()) }
            .sortedBy { chainLink -> chainLink.name.value }

        chainLinkSearchListState.clear()
        chainLinkSearchListState.addAll(chainLinks)
    }

    fun endSearch() {
        isSearchState.value = false
        searchKeywordState.value = ""

        chainLinkSearchListState.clear()
    }

    suspend fun getAll() = chainRepository.key(chain!!.id).mapCatching { chainKeyEntity ->
        val secretKey = PasswordEncoder.hash(EncoderSpec.Passphrase(
            PasswordEncoder.Base64.encode(chain!!.key.value.encodeToByteArray()),
            PasswordEncoder.Base64.encode(chain!!.name.value.encodeToByteArray())
        ))

        val privateKey = PasswordEncoder.encrypt(
            PasswordEncoder.Base64.encode(chain!!.key.value.encodeToByteArray()),
            EncoderSpec.Passphrase(secretKey, PasswordEncoder.Base64.encode(chain!!.name.value.encodeToByteArray()))
        )

        val saltKey = PasswordEncoder.hash(EncoderSpec.Passphrase(privateKey, chainKeyEntity.key))

        chainLinkRepository.read(ChainKeyEntity(chain!!.id, saltKey)).getOrThrow()
    }.map { chainLinkEntities ->
        chainLinks = chainLinkEntities.map { chainLinkEntity ->
            ChainLink().apply {
                id = chainLinkEntity.id
                name = ChainLink.Name(chainLinkEntity.name)
                description = ChainLink.Description(chainLinkEntity.description)
                password = ChainLink.Password(chainLinkEntity.password)
                status = ChainLink.Status.ACTUAL
            }
        }

        val draftChainLinks = chainLinkListState.filter { chainLink -> chainLink.status == ChainLink.Status.DRAFT }

        val chainLinks = chainLinks.map { chainLink ->
            chainLinkListState.firstOrNull { chainLink.id == it.id } ?: chainLink
        }.sortedBy { chainLink -> chainLink.name.value }.plus(draftChainLinks)

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)
    }

    suspend fun new(chainLink: ChainLink) = chainRepository.key(chain!!.id).mapCatching { chainKeyEntity ->
        val secretKey = PasswordEncoder.hash(EncoderSpec.Passphrase(
            PasswordEncoder.Base64.encode(chain!!.key.value.encodeToByteArray()),
            PasswordEncoder.Base64.encode(chain!!.name.value.encodeToByteArray())
        ))

        val privateKey = PasswordEncoder.encrypt(
            PasswordEncoder.Base64.encode(chain!!.key.value.encodeToByteArray()),
            EncoderSpec.Passphrase(secretKey, PasswordEncoder.Base64.encode(chain!!.name.value.encodeToByteArray()))
        )

        val saltKey = PasswordEncoder.hash(EncoderSpec.Passphrase(privateKey, chainKeyEntity.key))

        val privatePassword = PasswordEncoder.encrypt(
            PasswordEncoder.Base64.encode(chainLink.password.value.encodeToByteArray()),
            EncoderSpec.Passphrase(secretKey, PasswordEncoder.Base64.encode(chainLink.name.value.encodeToByteArray()))
        )

        chainLink.password = ChainLink.Password(privatePassword)

        val chainLinkEntity = ChainLinkEntity(
            chainLink.id,
            chainLink.name.value,
            chainLink.description.value,
            chainLink.password.value,
            ChainKeyEntity(chain!!.id, saltKey)
        )

        chainLinkRepository.create(chainLinkEntity).getOrThrow()
    }.map {
        chainLink.status = ChainLink.Status.ACTUAL

        update()
    }

    suspend fun edit(chainLink: ChainLink) = chainRepository.key(chain!!.id).mapCatching { chainKeyEntity ->
        val secretKey = PasswordEncoder.hash(EncoderSpec.Passphrase(
            PasswordEncoder.Base64.encode(chain!!.key.value.encodeToByteArray()),
            PasswordEncoder.Base64.encode(chain!!.name.value.encodeToByteArray())
        ))

        val privateKey = PasswordEncoder.encrypt(
            PasswordEncoder.Base64.encode(chain!!.key.value.encodeToByteArray()),
            EncoderSpec.Passphrase(secretKey, PasswordEncoder.Base64.encode(chain!!.name.value.encodeToByteArray()))
        )

        val saltKey = PasswordEncoder.hash(EncoderSpec.Passphrase(privateKey, chainKeyEntity.key))

        val privatePassword = PasswordEncoder.encrypt(
            PasswordEncoder.Base64.encode(chainLink.password.value.encodeToByteArray()),
            EncoderSpec.Passphrase(secretKey, PasswordEncoder.Base64.encode(chainLink.name.value.encodeToByteArray()))
        )

        chainLink.password = ChainLink.Password(privatePassword)

        val chainLinkEntity = ChainLinkEntity(
            chainLink.id,
            chainLink.name.value,
            chainLink.description.value,
            chainLink.password.value,
            ChainKeyEntity(chain!!.id, saltKey)
        )

        chainLinkRepository.update(chainLinkEntity).getOrThrow()

        chainLink.status = ChainLink.Status.ACTUAL

        update()
    }

    suspend fun remove(chainLink: ChainLink) = chainRepository.key(chain!!.id).mapCatching { chainKeyEntity ->
        val secretKey = PasswordEncoder.hash(EncoderSpec.Passphrase(
            PasswordEncoder.Base64.encode(chain!!.key.value.encodeToByteArray()),
            PasswordEncoder.Base64.encode(chain!!.name.value.encodeToByteArray())
        ))

        val privateKey = PasswordEncoder.encrypt(
            PasswordEncoder.Base64.encode(chain!!.key.value.encodeToByteArray()),
            EncoderSpec.Passphrase(secretKey, PasswordEncoder.Base64.encode(chain!!.name.value.encodeToByteArray()))
        )

        val saltKey = PasswordEncoder.hash(EncoderSpec.Passphrase(privateKey, chainKeyEntity.key))

        val chainLinkEntity = ChainLinkEntity(
            chainLink.id,
            chainLink.name.value,
            chainLink.description.value,
            chainLink.password.value,
            ChainKeyEntity(chain!!.id, saltKey)
        )

        chainLinkRepository.delete(chainLinkEntity).getOrThrow()

        update()
    }

    private fun update() {
        val draftChainLinks = chainLinkListState.filter { chainLink -> chainLink.status == ChainLink.Status.DRAFT }

        chainLinks = chainLinkListState.filter { chainLink -> chainLink.status != ChainLink.Status.DRAFT }

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks.sortedBy { chainLink -> chainLink.name.value }.plus(draftChainLinks))
    }
}