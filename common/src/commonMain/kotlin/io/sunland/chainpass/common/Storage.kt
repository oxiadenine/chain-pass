package io.sunland.chainpass.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

expect class Storage(dirPath: String, options: StorageOptions = StorageOptions()) {
    val dirPath: String
    val options: StorageOptions

    fun store(storable: Storable): String
    fun unstore(filePath: String): Storable
}

enum class StorageType { JSON, CSV, TXT }

data class StorageOptions(val isPrivate: Boolean = true, val type: StorageType = StorageType.JSON)

@Serializable
data class Storable(val id: String, val options: Map<String, String>, val items: List<Map<String, String>>)

fun Storable.toString(options: StorageOptions) = when (options.type) {
    StorageType.JSON -> Json.encodeToString(value = this)
    StorageType.CSV -> buildString {
        val optionsHeader = this@toString.options.keys.toSet().joinToString(",")

        append("$optionsHeader\n")

        val optionsRecord = this@toString.options.values.joinToString(",")

        append("$optionsRecord\n")

        val itemsHeader = this@toString.items.flatMap { item -> item.keys }.toSet().joinToString(",")

        append("$itemsHeader\n")

        val itemsRecords = this@toString.items.map { item ->
            item.values.joinToString(",") { field ->
                "\"${field.replace("\"", "\"\"")}\""
            }
        }

        itemsRecords.forEach { record -> append("$record\n") }
    }
    StorageType.TXT -> buildString {
        append(this@toString.options)
        append(this@toString.items)
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

        val itemsHeader = data[2].split(",")

        if (itemsHeader.size != 4) {
            isFormatValid = false
        }

        val items = mutableListOf<Map<String, String>>()

        for (i in 3 until data.size - 1) {
            val item = mutableMapOf<String, String>()

            val itemsRecord = data[i].split(",")

            if (itemsRecord.size != 4) {
                isFormatValid = false
            }

            for (j in itemsHeader.indices) {
                item[itemsHeader[j]] = itemsRecord[j].substringAfter("\"").substringBeforeLast("\"")
            }

            items.add(item)
        }

        if (!isFormatValid) {
            throw IllegalArgumentException("Invalid $storageType file format")
        }

        Storable("", options, items)
    }
    StorageType.TXT -> throw IllegalArgumentException("Storage type $storageType not supported")
}