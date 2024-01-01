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

    val chainLinks = mutableStateListOf<ChainLink>()

    suspend fun getAll(): Result<Unit> {
        return chainRepository.key(chain!!.id).mapCatching { key ->
            var passphrase = EncoderSpec.Passphrase(chain!!.key.value, chain!!.name.value)

            passphrase = EncoderSpec.Passphrase(PasswordEncoder.encrypt(passphrase, chain!!.key.value), key.key)

            val chainKeyEntity = ChainKeyEntity(chain!!.id, PasswordEncoder.hash(passphrase))

            chainLinkRepository.read(chainKeyEntity).getOrThrow()
        }.mapCatching { chainLinkEntities ->
            val passphrase = EncoderSpec.Passphrase(chain!!.key.value, chain!!.name.value)

            this.chainLinks.clear()
            this.chainLinks.addAll(chainLinkEntities.map { chainLinkEntity ->
                ChainLink().apply {
                    id = chainLinkEntity.id
                    name = ChainLink.Name(chainLinkEntity.name)
                    password = ChainLink.Password(PasswordEncoder.decrypt(passphrase, chainLinkEntity.password))
                    status = ChainLinkStatus.ACTUAL
                }
            })
        }
    }

    fun draft() {
        val chainLink = ChainLink()

        chainLinks.add(chainLink)
    }

    fun rejectDraft(chainLink: ChainLink) {
        chainLinks.remove(chainLink)
    }

    suspend fun new(chainLink: ChainLink): Result<Unit> {
        return chainRepository.key(chain!!.id).mapCatching { key ->
            var passphrase = EncoderSpec.Passphrase(chain!!.key.value, chain!!.name.value)

            passphrase = EncoderSpec.Passphrase(PasswordEncoder.encrypt(passphrase, chain!!.key.value), key.key)

            val chainKeyEntity = ChainKeyEntity(chain!!.id, PasswordEncoder.hash(passphrase))

            passphrase = EncoderSpec.Passphrase(chain!!.key.value, chain!!.name.value)

            chainLink.password = ChainLink.Password(PasswordEncoder.encrypt(passphrase, chainLink.password.value))

            val chainLinkEntity = ChainLinkEntity(chainLink.id, chainLink.name.value, chainLink.password.value, chainKeyEntity)

            chainLinkRepository.create(chainLinkEntity).getOrThrow()
        }.mapCatching { chainLinkId ->
            val passphrase = EncoderSpec.Passphrase(chain!!.key.value, chain!!.name.value)

            chainLink.id = chainLinkId
            chainLink.password = ChainLink.Password(PasswordEncoder.decrypt(passphrase, chainLink.password.value))
            chainLink.status = ChainLinkStatus.ACTUAL

            val chainLinks = chainLinks.toList()

            this.chainLinks.clear()
            this.chainLinks.addAll(chainLinks)
        }
    }
    
    fun startEdit(chainLinkId: Int) {
        val chainLinks = chainLinks.map { chainLink ->
            if (chainLink.status == ChainLinkStatus.EDIT) {
                chainLink.status = ChainLinkStatus.ACTUAL
            }

            if (chainLink.id == chainLinkId) {
                chainLink.status = ChainLinkStatus.EDIT
            }

            chainLink
        }.toList()

        this.chainLinks.clear()
        this.chainLinks.addAll(chainLinks)
    }

    fun endEdit(chainLink: ChainLink) {
        chainLink.status = ChainLinkStatus.ACTUAL

        val chainLinks = chainLinks.toList()

        this.chainLinks.clear()
        this.chainLinks.addAll(chainLinks)
    }

    suspend fun edit(chainLink: ChainLink): Result<Unit> {
        return chainRepository.key(chain!!.id).mapCatching { key ->
            var passphrase = EncoderSpec.Passphrase(chain!!.key.value, chain!!.name.value)

            passphrase = EncoderSpec.Passphrase(PasswordEncoder.encrypt(passphrase, chain!!.key.value), key.key)

            val chainKeyEntity = ChainKeyEntity(chain!!.id, PasswordEncoder.hash(passphrase))

            passphrase = EncoderSpec.Passphrase(chain!!.key.value, chain!!.name.value)

            chainLink.password = ChainLink.Password(PasswordEncoder.encrypt(passphrase, chainLink.password.value))

            val chainLinkEntity = ChainLinkEntity(chainLink.id, chainLink.name.value, chainLink.password.value, chainKeyEntity)

            chainLinkRepository.update(chainLinkEntity).getOrThrow()
        }.mapCatching {
            val passphrase = EncoderSpec.Passphrase(chain!!.key.value, chain!!.name.value)

            chainLink.password = ChainLink.Password(PasswordEncoder.decrypt(passphrase, chainLink.password.value))

            endEdit(chainLink)
        }
    }

    fun remove(chainLink: ChainLink, onRemove: (ChainLink) -> Unit) {
        chainLinks.remove(chainLink)

        onRemove(chainLink)
    }

    fun undoRemove(chainLink: ChainLink) {
        chainLinks.add(chainLink)
    }

    suspend fun remove(chainLink: ChainLink): Result<Unit> {
        return chainRepository.key(chain!!.id).mapCatching { key ->
            var passphrase = EncoderSpec.Passphrase(chain!!.key.value, chain!!.name.value)

            passphrase = EncoderSpec.Passphrase(PasswordEncoder.encrypt(passphrase, chain!!.key.value), key.key)

            val chainKeyEntity = ChainKeyEntity(chain!!.id, PasswordEncoder.hash(passphrase))

            passphrase = EncoderSpec.Passphrase(chain!!.key.value, chain!!.name.value)

            chainLink.password = ChainLink.Password(PasswordEncoder.encrypt(passphrase, chainLink.password.value))

            val chainLinkEntity = ChainLinkEntity(chainLink.id, chainLink.name.value, chainLink.password.value, chainKeyEntity)

            chainLinkRepository.delete(chainLinkEntity).getOrThrow()
        }
    }
}
