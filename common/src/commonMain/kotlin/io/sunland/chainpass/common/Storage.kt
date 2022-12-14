package io.sunland.chainpass.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

expect class Storage(dirPath: String, options: StorageOptions = StorageOptions()) {
    val dirPath: String
    val options: StorageOptions

    fun store(storable: Storable): String
    fun unstore(filePath: String): Storable
}

enum class StorageType { JSON, CSV, TXT }

data class StorageOptions(val isPrivate: Boolean = true, val type: StorageType = StorageType.JSON)

@Serializable
data class Storable(
    val options: Map<String, String>,
    val chain: Map<String, String>,
    val chainLinks: List<Map<String, String>>
)

fun Storable.toString(options: StorageOptions) = when (options.type) {
    StorageType.JSON -> Json.encodeToString(value = this)
    StorageType.CSV -> buildString {
        val optionsHeader = this@toString.options.keys.toSet().joinToString(",")

        append("$optionsHeader\n")

        val optionsRecord = this@toString.options.values.joinToString(",")

        append("$optionsRecord\n")

        val chainHeader = this@toString.chain.keys.toSet().joinToString(",")

        append("$chainHeader\n")

        val chainRecord = this@toString.chain.values.joinToString(",") { value ->
            "\"${value.replace("\"", "\"\"")}\""
        }

        append("$chainRecord\n")

        val chainLinkHeader = this@toString.chainLinks.flatMap { chainLink -> chainLink.keys }.toSet().joinToString(",")

        append("$chainLinkHeader\n")

        val chainLinkRecords = this@toString.chainLinks.map { chainLink ->
            chainLink.values.joinToString(",") { value ->
                "\"${value.replace("\"", "\"\"")}\""
            }
        }

        chainLinkRecords.forEach { chainLinkRecord -> append("$chainLinkRecord\n") }
    }
    StorageType.TXT -> buildString {
        append("${this@toString.options}\n")
        append("${this@toString.chain}\n")
        append("${this@toString.chainLinks}\n")
    }
}

fun String.toStorable(storageType: StorageType) = when (storageType) {
    StorageType.JSON -> Json.decodeFromString(this)
    StorageType.CSV -> {
        var isFormatValid = true

        val data = this.split("\n")

        val optionsHeader = data[0].split(",")
        val optionsRecord = data[1].split(",")

        if (optionsHeader.size != 1 || optionsRecord.size != 1) {
            isFormatValid = false
        }

        val options = mutableMapOf<String, String>()

        for (i in optionsHeader.indices) {
            options[optionsHeader[i]] = optionsRecord[i]
        }

        val chainHeader = data[2].split(",")

        if (chainHeader.size != 2) {
            isFormatValid = false
        }

        val chain = mutableMapOf<String, String>()

        val chainRecord = data[3].split(",")

        if (chainRecord.size != 2) {
            isFormatValid = false
        }

        for (j in chainHeader.indices) {
            chain[chainHeader[j]] = chainRecord[j].substringAfter("\"").substringBeforeLast("\"")
        }

        val chainLinkHeader = data[4].split(",")

        if (chainLinkHeader.size != 3) {
            isFormatValid = false
        }

        val chainLinks = mutableListOf<Map<String, String>>()

        for (i in 5 until data.size - 1) {
            val chainLink = mutableMapOf<String, String>()

            val chainLinkRecord = data[i].split(",")

            if (chainLinkRecord.size != 3) {
                isFormatValid = false
            }

            for (j in chainLinkHeader.indices) {
                chainLink[chainLinkHeader[j]] = chainLinkRecord[j].substringAfter("\"").substringBeforeLast("\"")
            }

            chainLinks.add(chainLink)
        }

        if (!isFormatValid) {
            throw IllegalArgumentException("Invalid $storageType file format")
        }

        Storable(options, chain, chainLinks)
    }
    StorageType.TXT -> throw IllegalArgumentException("Storage type $storageType not supported")
}