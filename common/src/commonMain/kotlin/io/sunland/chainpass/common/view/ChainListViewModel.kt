package io.sunland.chainpass.common.view

import androidx.compose.runtime.mutableStateListOf
import io.sunland.chainpass.common.Chain
import io.sunland.chainpass.common.ChainStatus
import io.sunland.chainpass.common.repository.ChainEntity
import io.sunland.chainpass.common.repository.ChainKeyEntity
import io.sunland.chainpass.common.repository.ChainRepository
import io.sunland.chainpass.common.security.EncoderSpec
import io.sunland.chainpass.common.security.PasswordEncoder

class ChainListViewModel(private val repository: ChainRepository) {
    private var _chains = emptyList<Chain>()
    val chains = mutableStateListOf<Chain>()

    suspend fun load() = runCatching {
        _chains = getAll().getOrThrow()

        val draftChains = chains.filter { chain -> chain.status == ChainStatus.DRAFT }

        chains.clear()
        chains.addAll(_chains.plus(draftChains))

        Unit
    }

    fun draft() {
        val chain = Chain()

        chains.add(chain)
    }

    fun rejectDraft(chain: Chain) {
        chains.remove(chain)

        update()
    }

    suspend fun new(chain: Chain): Result<Unit> {
        val chainKey =
            PasswordEncoder.hash(EncoderSpec.Passphrase(chain.key.value, chain.name.value)).let { secretKey ->
                Chain.Key(PasswordEncoder.encrypt(EncoderSpec.Passphrase(secretKey, chain.name.value), secretKey))
            }

        val chainEntity = ChainEntity(chain.id, chain.name.value, chainKey.value)

        chain.key = Chain.Key()

        return repository.create(chainEntity).map { chainId ->
            chain.id = chainId
            chain.status = ChainStatus.ACTUAL

            update()
        }
    }

    fun remove(chain: Chain, onItemRemove: (Chain) -> Unit): Chain {
        val passphrase = EncoderSpec.Passphrase(chain.key.value, chain.name.value)

        chain.key = Chain.Key()

        return Chain().apply {
            id = chain.id
            name = chain.name
            key = Chain.Key(PasswordEncoder.hash(passphrase))
            status = chain.status

            chains.remove(chain)

            onItemRemove(this)
        }
    }


    fun undoRemove(chain: Chain) {
        chain.key = Chain.Key()

        chains.add(chain)

        update()
    }

    suspend fun remove(chain: Chain): Result<Unit> {
        return repository.key(chain.id).mapCatching { key ->
            var passphrase = EncoderSpec.Passphrase(chain.key.value, chain.name.value)

            passphrase = EncoderSpec.Passphrase(PasswordEncoder.encrypt(passphrase, chain.key.value), key.key)

            val chainKeyEntity = ChainKeyEntity(chain.id, PasswordEncoder.hash(passphrase))

            repository.delete(chainKeyEntity).getOrThrow()

            update()
        }
    }

    fun select(chain: Chain, onItemSelect: (Chain) -> Unit): Chain {
        val passphrase = EncoderSpec.Passphrase(chain.key.value, chain.name.value)

        chain.key = Chain.Key()

        return Chain().apply {
            id = chain.id
            name = chain.name
            key = Chain.Key(PasswordEncoder.hash(passphrase))
            status = chain.status

            onItemSelect(this)
        }
    }

    private suspend fun getAll() = repository.read().map { chainEntities ->
        chainEntities.map { chainEntity ->
            Chain().apply {
                id = chainEntity.id
                name = Chain.Name(chainEntity.name)
                status = ChainStatus.ACTUAL
            }
        }
    }

    private fun update() {
        val draftChains = chains.filter { chain -> chain.status == ChainStatus.DRAFT }

        _chains = chains.filter { chain -> chain.status != ChainStatus.DRAFT }

        chains.clear()
        chains.addAll(_chains.plus(draftChains))
    }
}
