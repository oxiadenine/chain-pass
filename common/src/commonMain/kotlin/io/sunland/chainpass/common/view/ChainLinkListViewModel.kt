package io.sunland.chainpass.common.view

import androidx.compose.runtime.mutableStateListOf
import io.sunland.chainpass.common.Chain
import io.sunland.chainpass.common.ChainLink
import io.sunland.chainpass.common.ChainLinkStatus
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

    var _chainLinks = emptyList<ChainLink>()
    val chainLinks = mutableStateListOf<ChainLink>()

    suspend fun load() = runCatching {
        _chainLinks = getAll().getOrThrow()

        val draftChainLinks = chainLinks.filter { chainLink -> chainLink.status == ChainLinkStatus.DRAFT }
        val editChainLink = chainLinks.firstOrNull { chainLink -> chainLink.status == ChainLinkStatus.EDIT }

        chainLinks.clear()
        chainLinks.addAll(if (editChainLink != null) {
            _chainLinks.map { chainLink ->
                if (chainLink.id == editChainLink.id) {
                    editChainLink
                } else chainLink
            }.plus(draftChainLinks)
        } else _chainLinks.plus(draftChainLinks))

        Unit
    }

    fun draft() {
        val chainLink = ChainLink()

        chainLinks.add(chainLink)
    }

    fun rejectDraft(chainLink: ChainLink) {
        chainLinks.remove(chainLink)

        update()
    }

    suspend fun new(chainLink: ChainLink): Result<Unit> {
        return chainRepository.key(chain!!.id).mapCatching { key ->
            var passphrase = EncoderSpec.Passphrase(chain!!.key.value, chain!!.name.value)

            passphrase = EncoderSpec.Passphrase(PasswordEncoder.encrypt(passphrase, chain!!.key.value), key.key)

            val chainKeyEntity = ChainKeyEntity(chain!!.id, PasswordEncoder.hash(passphrase))

            passphrase = EncoderSpec.Passphrase(chain!!.key.value, chain!!.name.value)

            chainLink.password = ChainLink.Password(PasswordEncoder.encrypt(passphrase, chainLink.password.value))

            val chainLinkEntity = ChainLinkEntity(
                chainLink.id,
                chainLink.name.value,
                chainLink.description.value,
                chainLink.password.value,
                chainKeyEntity
            )

            chainLinkRepository.create(chainLinkEntity).getOrThrow()
        }.map { chainLinkId ->
            val passphrase = EncoderSpec.Passphrase(chain!!.key.value, chain!!.name.value)

            chainLink.id = chainLinkId
            chainLink.password = ChainLink.Password(PasswordEncoder.decrypt(passphrase, chainLink.password.value))
            chainLink.status = ChainLinkStatus.ACTUAL

            update()
        }
    }

    fun startEdit(chainLinkId: Int) {
        val draftChainLinks = chainLinks.filter { chainLink -> chainLink.status == ChainLinkStatus.DRAFT }

        chainLinks.clear()
        chainLinks.addAll(_chainLinks.map { chainLink ->
            if (chainLink.status == ChainLinkStatus.EDIT) {
                chainLink.status = ChainLinkStatus.ACTUAL
            }

            if (chainLink.id == chainLinkId) {
                chainLink.status = ChainLinkStatus.EDIT
            }

            chainLink
        }.plus(draftChainLinks))
    }

    fun endEdit(chainLink: ChainLink) {
        chainLink.status = ChainLinkStatus.ACTUAL

        update()
    }

    suspend fun edit(chainLink: ChainLink): Result<Unit> {
        return chainRepository.key(chain!!.id).mapCatching { key ->
            var passphrase = EncoderSpec.Passphrase(chain!!.key.value, chain!!.name.value)

            passphrase = EncoderSpec.Passphrase(PasswordEncoder.encrypt(passphrase, chain!!.key.value), key.key)

            val chainKeyEntity = ChainKeyEntity(chain!!.id, PasswordEncoder.hash(passphrase))

            passphrase = EncoderSpec.Passphrase(chain!!.key.value, chain!!.name.value)

            chainLink.password = ChainLink.Password(PasswordEncoder.encrypt(passphrase, chainLink.password.value))

            val chainLinkEntity = ChainLinkEntity(
                chainLink.id,
                chainLink.name.value,
                chainLink.description.value,
                chainLink.password.value,
                chainKeyEntity
            )

            chainLinkRepository.update(chainLinkEntity).getOrThrow()
        }.map {
            val passphrase = EncoderSpec.Passphrase(chain!!.key.value, chain!!.name.value)

            chainLink.password = ChainLink.Password(PasswordEncoder.decrypt(passphrase, chainLink.password.value))

            endEdit(chainLink)
        }
    }

    fun remove(chainLink: ChainLink, onRemove: (Chain, ChainLink) -> Unit) {
        chainLinks.remove(chainLink)

        val chain = Chain().apply {
            id = chain!!.id
            name = chain!!.name
            key = chain!!.key
            status = chain!!.status
        }

        onRemove(chain, chainLink)
    }

    fun undoRemove(chainLink: ChainLink) {
        chainLinks.add(chainLink)

        update()
    }

    suspend fun remove(chain: Chain, chainLink: ChainLink): Result<Unit> {
        return chainRepository.key(chain.id).mapCatching { key ->
            var passphrase = EncoderSpec.Passphrase(chain.key.value, chain.name.value)

            passphrase = EncoderSpec.Passphrase(PasswordEncoder.encrypt(passphrase, chain.key.value), key.key)

            val chainKeyEntity = ChainKeyEntity(chain.id, PasswordEncoder.hash(passphrase))

            passphrase = EncoderSpec.Passphrase(chain.key.value, chain.name.value)

            chainLink.password = ChainLink.Password(PasswordEncoder.encrypt(passphrase, chainLink.password.value))

            val chainLinkEntity = ChainLinkEntity(
                chainLink.id,
                chainLink.name.value,
                chainLink.description.value,
                chainLink.password.value,
                chainKeyEntity
            )

            chainLinkRepository.delete(chainLinkEntity).getOrThrow()

            update()
        }
    }

    private suspend fun getAll() = chainRepository.key(chain!!.id).mapCatching { key ->
        var passphrase = EncoderSpec.Passphrase(chain!!.key.value, chain!!.name.value)

        passphrase = EncoderSpec.Passphrase(PasswordEncoder.encrypt(passphrase, chain!!.key.value), key.key)

        val chainKeyEntity = ChainKeyEntity(chain!!.id, PasswordEncoder.hash(passphrase))

        chainLinkRepository.read(chainKeyEntity).getOrThrow()
    }.map { chainLinkEntities ->
        val passphrase = EncoderSpec.Passphrase(chain!!.key.value, chain!!.name.value)

        chainLinkEntities.map { chainLinkEntity ->
            ChainLink().apply {
                id = chainLinkEntity.id
                name = ChainLink.Name(chainLinkEntity.name)
                description = ChainLink.Description(chainLinkEntity.description)
                password = ChainLink.Password(PasswordEncoder.decrypt(passphrase, chainLinkEntity.password))
                status = ChainLinkStatus.ACTUAL
            }
        }
    }

    private fun update() {
        val draftChainLinks = chainLinks.filter { chainLink -> chainLink.status == ChainLinkStatus.DRAFT }

        _chainLinks = chainLinks.filter { chainLink -> chainLink.status != ChainLinkStatus.DRAFT }

        chainLinks.clear()
        chainLinks.addAll(_chainLinks.plus(draftChainLinks))
    }
}
