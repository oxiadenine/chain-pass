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
    }

    fun rejectDraft(chainLink: ChainLink) {
        chainLinkListState.remove(chainLink)
    }

    fun rejectDrafts() {
        val chainLinks = chainLinkListState.filter { chainLink -> chainLink.status != ChainLink.Status.DRAFT }

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)
    }

    fun startEdit(chainLinkEdit: ChainLink) {
        val chainLinksDraft = chainLinkListState.filter { chainLink -> chainLink.status == ChainLink.Status.DRAFT }

        val chainLinks = chainLinkListState
            .filter { chainLink -> chainLink.status != ChainLink.Status.DRAFT }
            .map { chainLink ->
                if (chainLink.id == chainLinkEdit.id) {
                    if (chainLink.password.isPrivate) {
                        unlockPassword(chainLink)
                    }

                    chainLink.status = ChainLink.Status.EDIT
                }

                chainLink
            }
            .sortedBy { chainLink -> chainLink.name.value }
            .plus(chainLinksDraft)

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)
    }

    fun cancelEdit(chainLinkEdit: ChainLink) {
        val chainLinksDraft = chainLinkListState.filter { chainLink -> chainLink.status == ChainLink.Status.DRAFT }

        val chainLinks = chainLinkListState
            .filter { chainLink -> chainLink.status != ChainLink.Status.DRAFT }
            .map { chainLink ->
                if (chainLink.id == chainLinkEdit.id && chainLink.status == ChainLink.Status.EDIT) {
                    lockPassword(chainLink)

                    chainLink.status = ChainLink.Status.ACTUAL
                }

                chainLink
            }
            .sortedBy { chainLink -> chainLink.name.value }
            .plus(chainLinksDraft)

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)
    }

    fun cancelEdits() {
        val chainLinksDraft = chainLinkListState.filter { chainLink -> chainLink.status == ChainLink.Status.DRAFT }

        val chainLinks = chainLinkListState
            .filter { chainLink -> chainLink.status != ChainLink.Status.DRAFT }
            .map { chainLink ->
                if (chainLink.status == ChainLink.Status.EDIT) {
                    lockPassword(chainLink)

                    chainLink.status = ChainLink.Status.ACTUAL
                }

                chainLink
            }
            .sortedBy { chainLink -> chainLink.name.value }
            .plus(chainLinksDraft)

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)
    }

    fun removeLater(chainLink: ChainLink) {
        chainLinkListState.remove(chainLink)
    }

    fun undoRemove(chainLinkRemove: ChainLink) {
        val chainLinksDraft = chainLinkListState.filter { chainLink -> chainLink.status == ChainLink.Status.DRAFT }

        val chainLinks = chainLinkListState
            .filter { chainLink -> chainLink.status != ChainLink.Status.DRAFT }
            .plus(chainLinkRemove)
            .sortedBy { chainLink -> chainLink.name.value }
            .plus(chainLinksDraft)

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)
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

        val chainLinksDraft = chainLinkListState.filter { chainLink -> chainLink.status == ChainLink.Status.DRAFT }

        val chainLinks = chainLinks
            .map { chainLink ->
                if (chainLinkListState.isEmpty()) {
                    ChainLink(chainLink)
                } else {
                    chainLinkListState.first { chainLink.id == it.id }.let {
                        if (it.status == ChainLink.Status.EDIT) {
                            ChainLink(it)
                        } else ChainLink(chainLink)
                    }
                }
            }
            .sortedBy { chainLink -> chainLink.name.value }
            .plus(chainLinksDraft)

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)

        Unit
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
        val chainLinksRemove = chainLinks.filter { chainLink -> !chainLinkListState.any { chainLink.id == it.id } }

        chainLinks = chainLinkListState
            .filter { chainLink -> chainLink.status != ChainLink.Status.DRAFT }
            .map { chainLink ->
                if (chainLink.status == ChainLink.Status.EDIT) {
                    chainLinks.first { chainLink.id == it.id }
                } else ChainLink(chainLink)
            }
            .plus(chainLinksRemove)

        val chainLinksDraft = chainLinkListState.filter { chainLink -> chainLink.status == ChainLink.Status.DRAFT }

        val chainLinks = chainLinkListState
            .filter { chainLink -> chainLink.status != ChainLink.Status.DRAFT }
            .sortedBy { chainLink -> chainLink.name.value }
            .plus(chainLinksDraft)

        chainLinkListState.clear()
        chainLinkListState.addAll(chainLinks)
    }
}