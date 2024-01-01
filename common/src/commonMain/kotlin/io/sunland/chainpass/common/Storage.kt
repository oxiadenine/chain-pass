package io.sunland.chainpass.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

enum class StorageType { JSON, CSV, TXT }

class Storage(dirPath: String) {
    val storePath = "$dirPath/Chain Pass/Store"

    init {
        if (!File(storePath).exists()) {
            File(storePath).mkdirs()
        }
    }

    fun store(storageType: StorageType, storable: Storable): String {
        val date = (DateFormat.getDateTimeInstance() as SimpleDateFormat).apply {
            applyPattern("yyyy.MM.dd-HH.mm.ss")
        }.format(Date())

        val fileName = "chains-$date"

        val filePath = when (storageType) {
            StorageType.JSON -> "$storePath/$fileName.json"
            StorageType.CSV -> "$storePath/$fileName.csv"
            StorageType.TXT -> "$storePath/$fileName.txt"
        }

        if (!File(filePath).exists()) {
            File(filePath).createNewFile()
        }

        File(filePath).writeText(storable.toString(storageType))

        return fileName
    }

    fun unstore(filePath: String, fileBytes: ByteArray): Storable {
        if (!File(filePath).exists()) {
            error("File $filePath does not exists")
        }

        val storageType = StorageType.valueOf(File(filePath).extension.uppercase())

        return fileBytes.decodeToString().toStorable(storageType)
    }
}

@Serializable
data class StorableOptions(val isPrivate: Boolean)

@Serializable
data class StorableChain(val name: String, val key: String, val chainLinks: List<StorableChainLink>)

@Serializable
data class StorableChainLink(val name: String, val description: String, val password: String)

@Serializable
data class Storable(val options: StorableOptions, val chains: List<StorableChain>)

fun Storable.toString(storageType: StorageType) = when (storageType) {
    StorageType.JSON -> Json.encodeToString(value = this)
    StorageType.CSV -> buildString {
        val optionsHeader = StorableOptions::class.primaryConstructor!!.parameters
            .joinToString(",") { parameter -> parameter.name!! }

        append("$optionsHeader\n")

        val optionsRecord = options::class.primaryConstructor!!.parameters
            .map { parameter ->
                options::class.memberProperties.first { property ->
                    property.name == parameter.name
                }.getter.call(options)
            }
            .joinToString(",")

        append("$optionsRecord\n")

        chains.forEach { chain ->
            val chainHeader = StorableChain::class.primaryConstructor!!.parameters
                .dropLast(1)
                .joinToString(",") { property -> property.name!! }

            append("$chainHeader\n")

            val chainRecord = chain::class.primaryConstructor!!.parameters
                .dropLast(1)
                .map { parameter ->
                    chain::class.memberProperties.first { property ->
                        property.name == parameter.name
                    }.getter.call(chain)
                }
                .joinToString(",") { value ->
                    "\"${value.toString().replace("\"", "\"\"")}\""
                }

            append("$chainRecord\n")

            val chainLinkHeader = StorableChainLink::class.primaryConstructor!!.parameters
                .joinToString(",") { property -> property.name!! }

            append("$chainLinkHeader\n")

            chain.chainLinks.map { chainLink ->
                val chainLinkRecord = chainLink::class.primaryConstructor!!.parameters
                    .map { parameter ->
                        chainLink::class.memberProperties.first { property ->
                            property.name == parameter.name
                        }.getter.call(chainLink)
                    }
                    .joinToString(",") { value ->
                        "\"${value.toString().replace("\"", "\"\"")}\""
                    }

                append("$chainLinkRecord\n")
            }
        }
    }
    StorageType.TXT -> buildString {
        append("${options}\n")
        append("${chains}\n")
    }
}

fun String.toStorable(storageType: StorageType) = when (storageType) {
    StorageType.JSON -> Json.decodeFromString(this)
    StorageType.CSV -> {
        val optionsHeader = StorableOptions::class.primaryConstructor!!.parameters
            .map { parameter -> parameter.name!! }

        val chainHeader = StorableChain::class.primaryConstructor!!.parameters
            .dropLast(1).map { property -> property.name!! }

        val chainLinkHeader = StorableChainLink::class.primaryConstructor!!.parameters
            .map { property -> property.name!! }

        val data = split("\n")

        var record = data[0].split(",")

        val storableOptions = if (record.size == optionsHeader.size && record[0] == optionsHeader[0]) {
            val optionsRecord = data[1].split(",")

            StorableOptions(optionsRecord[0].toBoolean())
        } else error("Invalid $storageType format")

        val storableChains = mutableListOf<StorableChain>()

        for (i in 2 until data.size - 1) {
            record = data[i].split(",")

            if (record.size == chainHeader.size && record[0] == chainHeader[0] && record[1] == chainHeader[1]) {
                record = data[i + 1].split(",")

                val chainRecord = record.map { value ->
                    value.replace("\"", "")
                }

                record = data[i + 2].split(",")

                val storableChainLinks = mutableListOf<StorableChainLink>()

                if (record.size == chainLinkHeader.size && record[0] == chainLinkHeader[0] &&
                    record[1] == chainLinkHeader[1] && record[2] == chainLinkHeader[2]) {

                    for (j in i + 3 until data.size - 1) {
                        record = data[j].split(",")

                        if (record.size == 3) {
                            val chainLinkRecord = record.map { value ->
                                value.replace("\"", "")
                            }

                            storableChainLinks.add(
                                StorableChainLink(chainLinkRecord[0], chainLinkRecord[1], chainLinkRecord[2])
                            )
                        } else break
                    }
                }

                storableChains.add(StorableChain(chainRecord[0], chainRecord[1], storableChainLinks))
            } else continue
        }

        Storable(storableOptions, storableChains)
    }
    else -> error("Storage type $storageType is not supported")
}