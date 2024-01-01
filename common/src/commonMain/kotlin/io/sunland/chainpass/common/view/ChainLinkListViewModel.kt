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

    private var chainLinks = emptyList<ChainLink>()

    fun draft() {
        chainLinkListState.add(ChainLink())
    }

    fun rejectDraft(chainLink: ChainLink) {
        chainLinkListState.remove(chainLink)

        update()
    }

    fun startEdit(chainLinkId: Int) {
        val draftChainLinks = chainLinkListState.filter { chainLink -> chainLink.status == ChainLink.Status.DRAFT }
        val editChainLinks = chainLinkListState.filter { chainLink -> chainLink.status != ChainLink.Status.DRAFT }

        chainLinkListState.clear()
        chainLinkListState.addAll(editChainLinks.map { chainLink ->
            if (chainLink.status == ChainLink.Status.EDIT) {
                lockPassword(chainLink)

                chainLink.status = ChainLink.Status.ACTUAL
            }

            if (chainLink.id == chainLinkId) {
                chainLink.status = ChainLink.Status.EDIT
            }

            chainLink
        }.sortedBy { chainLink -> chainLink.name.value }.plus(draftChainLinks))
    }

    fun endEdit(chainLink: ChainLink) {
        chainLink.status = ChainLink.Status.ACTUAL

        update()
    }

    fun removeLater(chainLink: ChainLink) {
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

        val password = PasswordEncoder.decrypt(EncoderSpec.Passphrase(
            secretKey,
            PasswordEncoder.Base64.encode(chainLink.name.value.encodeToByteArray())
        ), chainLink.password.value)

        chainLink.password = ChainLink.Password(PasswordEncoder.Base64.decode(password).decodeToString(), false)
    }

    fun lockPassword(chainLink: ChainLink) {
        val secretKey = PasswordEncoder.hash(EncoderSpec.Passphrase(
            PasswordEncoder.Base64.encode(chain!!.key.value.encodeToByteArray()),
            PasswordEncoder.Base64.encode(chain!!.name.value.encodeToByteArray())
        ))

        val privatePassword = PasswordEncoder.encrypt(EncoderSpec.Passphrase(
            secretKey,
            PasswordEncoder.Base64.encode(chainLink.name.value.encodeToByteArray())
        ), PasswordEncoder.Base64.encode(chainLink.password.value.encodeToByteArray()))

        chainLink.password = ChainLink.Password(privatePassword)
    }

    fun startSearch() {
        isSearchState.value = true

        chainLinkSearchListState.addAll(chainLinkListState.filter { chainLink ->
            chainLink.status == ChainLink.Status.ACTUAL
        }.sortedBy { chainLink -> chainLink.name.value })
    }

    fun search(keyword: String) {
        chainLinkSearchListState.clear()
        chainLinkSearchListState.addAll(chainLinkListState.filter { chainLink ->
            chainLink.name.value.lowercase().contains(keyword.lowercase()) &&
                    chainLink.status == ChainLink.Status.ACTUAL
        }.sortedBy { chainLink -> chainLink.name.value })
    }

    fun endSearch() {
        isSearchState.value = false

        chainLinkSearchListState.clear()
    }

    suspend fun getAll() = chainRepository.key(chain!!.id).mapCatching { chainKeyEntity ->
        val secretKey = PasswordEncoder.hash(EncoderSpec.Passphrase(
            PasswordEncoder.Base64.encode(chain!!.key.value.encodeToByteArray()),
            PasswordEncoder.Base64.encode(chain!!.name.value.encodeToByteArray())
        ))

        val privateKey = PasswordEncoder.encrypt(EncoderSpec.Passphrase(
            secretKey,
            PasswordEncoder.Base64.encode(chain!!.name.value.encodeToByteArray())
        ), chain!!.key.value)

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
        val editChainLink = chainLinkListState.firstOrNull { chainLink -> chainLink.status == ChainLink.Status.EDIT }

        chainLinkListState.clear()
        chainLinkListState.addAll(if (editChainLink != null) {
            chainLinks.map { chainLink ->
                if (chainLink.id == editChainLink.id) {
                    editChainLink
                } else chainLink
            }.sortedBy { chainLink -> chainLink.name.value }.plus(draftChainLinks)
        } else chainLinks.sortedBy { chainLink -> chainLink.name.value }.plus(draftChainLinks))
    }

    suspend fun new(chainLink: ChainLink) = chainRepository.key(chain!!.id).mapCatching { chainKeyEntity ->
        val secretKey = PasswordEncoder.hash(EncoderSpec.Passphrase(
            PasswordEncoder.Base64.encode(chain!!.key.value.encodeToByteArray()),
            PasswordEncoder.Base64.encode(chain!!.name.value.encodeToByteArray())
        ))

        val privateKey = PasswordEncoder.encrypt(EncoderSpec.Passphrase(
            secretKey,
            PasswordEncoder.Base64.encode(chain!!.name.value.encodeToByteArray())
        ), chain!!.key.value)

        val saltKey = PasswordEncoder.hash(EncoderSpec.Passphrase(privateKey, chainKeyEntity.key))

        val privatePassword = PasswordEncoder.encrypt(EncoderSpec.Passphrase(
            secretKey,
            PasswordEncoder.Base64.encode(chainLink.name.value.encodeToByteArray())
        ), PasswordEncoder.Base64.encode(chainLink.password.value.encodeToByteArray()))

        chainLink.password = ChainLink.Password(privatePassword)

        val chainLinkEntity = ChainLinkEntity(
            chainLink.id,
            chainLink.name.value,
            chainLink.description.value,
            chainLink.password.value,
            ChainKeyEntity(chain!!.id, saltKey)
        )

        chainLinkRepository.create(chainLinkEntity).getOrThrow()
    }.map { chainLinkId ->
        chainLink.id = chainLinkId
        chainLink.status = ChainLink.Status.ACTUAL

        update()
    }

    suspend fun edit(chainLink: ChainLink) = chainRepository.key(chain!!.id).mapCatching { chainKeyEntity ->
        val secretKey = PasswordEncoder.hash(EncoderSpec.Passphrase(
            PasswordEncoder.Base64.encode(chain!!.key.value.encodeToByteArray()),
            PasswordEncoder.Base64.encode(chain!!.name.value.encodeToByteArray())
        ))

        val privateKey = PasswordEncoder.encrypt(EncoderSpec.Passphrase(
            secretKey,
            PasswordEncoder.Base64.encode(chain!!.name.value.encodeToByteArray())
        ), chain!!.key.value)

        val saltKey = PasswordEncoder.hash(EncoderSpec.Passphrase(privateKey, chainKeyEntity.key))

        val privatePassword = PasswordEncoder.encrypt(EncoderSpec.Passphrase(
            secretKey,
            PasswordEncoder.Base64.encode(chainLink.name.value.encodeToByteArray())
        ), PasswordEncoder.Base64.encode(chainLink.password.value.encodeToByteArray()))

        chainLink.password = ChainLink.Password(privatePassword)

        val chainLinkEntity = ChainLinkEntity(
            chainLink.id,
            chainLink.name.value,
            chainLink.description.value,
            chainLink.password.value,
            ChainKeyEntity(chain!!.id, saltKey)
        )

        chainLinkRepository.update(chainLinkEntity).getOrThrow()

        endEdit(chainLink)
    }

    suspend fun remove(chainLink: ChainLink) = chainRepository.key(chain!!.id).mapCatching { chainKeyEntity ->
        val secretKey = PasswordEncoder.hash(EncoderSpec.Passphrase(
            PasswordEncoder.Base64.encode(chain!!.key.value.encodeToByteArray()),
            PasswordEncoder.Base64.encode(chain!!.name.value.encodeToByteArray())
        ))

        val privateKey = PasswordEncoder.encrypt(EncoderSpec.Passphrase(
            secretKey,
            PasswordEncoder.Base64.encode(chain!!.name.value.encodeToByteArray())
        ), chain!!.key.value)

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
