package io.sunland.chainpass.common.view

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import io.sunland.chainpass.common.Chain
import io.sunland.chainpass.common.repository.ChainEntity
import io.sunland.chainpass.common.repository.ChainKeyEntity
import io.sunland.chainpass.common.repository.ChainRepository
import io.sunland.chainpass.common.security.EncoderSpec
import io.sunland.chainpass.common.security.PasswordEncoder

class ChainListViewModel(private val repository: ChainRepository) {
    val chains = mutableStateListOf<Chain>()

    var chainToRemove = mutableStateOf<Chain?>(null)
    var chainToSelect = mutableStateOf<Chain?>(null)

    private var _chains = emptyList<Chain>()

    suspend fun load() = runCatching {
        _chains = getAll().getOrThrow()

        val draftChains = chains.filter { chain -> chain.status == Chain.Status.DRAFT }

        chains.clear()
        chains.addAll(_chains.sortedBy { chain -> chain.name.value }.plus(draftChains))

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
            chain.status = Chain.Status.ACTUAL

            update()
        }
    }

    fun remove() = chainToRemove.value?.let { chain ->
        chains.remove(chain)

        Chain().apply {
            id = chain.id
            name = chain.name
            key = Chain.Key(PasswordEncoder.hash(EncoderSpec.Passphrase(chain.key.value, chain.name.value)))
            status = chain.status
        }
    }

    fun undoRemove(chain: Chain) {
        chains.add(chain.apply { key = Chain.Key() })

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

    fun select() = chainToSelect.value?.let { chain ->
        Chain().apply {
            id = chain.id
            name = chain.name
            key = Chain.Key(PasswordEncoder.hash(EncoderSpec.Passphrase(chain.key.value, chain.name.value)))
            status = chain.status
        }
    }

    private suspend fun getAll() = repository.read().map { chainEntities ->
        chainEntities.map { chainEntity ->
            Chain().apply {
                id = chainEntity.id
                name = Chain.Name(chainEntity.name)
                status = Chain.Status.ACTUAL
            }
        }
    }

    private fun update() {
        val draftChains = chains.filter { chain -> chain.status == Chain.Status.DRAFT }

        _chains = chains.filter { chain -> chain.status != Chain.Status.DRAFT }

        chains.clear()
        chains.addAll(_chains.sortedBy { chain -> chain.name.value }.plus(draftChains))
    }
}
