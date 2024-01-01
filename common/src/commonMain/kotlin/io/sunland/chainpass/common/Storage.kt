package io.sunland.chainpass.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
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

    fun unstore(filePath: String): Storable {
        if (!File(filePath).exists()) {
            error("File $filePath does not exists")
        }

        val storageType = StorageType.valueOf(File(filePath).extension.uppercase())

        return File(filePath).readText().toStorable(storageType)
    }
}

@Serializable
data class StorableOptions(val isPrivate: Boolean, val isSingle: Boolean)

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
        val data = split("\n")

        val optionsRecord = data[1].split(",")

        val chainRecord = data[3].split(",").map { value ->
            value.replace("\"", "")
        }

        val chainLinkRecords = mutableListOf<Array<String>>()

        for (i in 5 until data.size - 1) {
            val chainLinkRecord = data[i].split(",").map { value ->
                value.replace("\"", "")
            }

            chainLinkRecords.add(chainLinkRecord.toTypedArray())
        }

        val storableOptions = StorableOptions(optionsRecord[0].toBoolean(), optionsRecord[1].toBoolean())
        val storableChainLinks = chainLinkRecords.map { chainLinkRecord ->
            StorableChainLink(chainLinkRecord[0], chainLinkRecord[1], chainLinkRecord[2])
        }
        val storableChain = StorableChain(chainRecord[0], chainRecord[1], storableChainLinks)

        Storable(storableOptions, listOf(storableChain))
    }
    else -> throw IllegalArgumentException("Storage type $storageType is not supported")
}