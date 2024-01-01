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

        val itemsHeader = this@toString.items.flatMap { it.keys }.toSet().joinToString(",")

        append("$itemsHeader\n")

        val itemsRecords = this@toString.items.map {
            it.values.joinToString(",") { field ->
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
        val data = this.split("\n")

        val optionsHeader = data[0].split(",")
        val optionsRecord = data[1].split(",")

        val options = mutableMapOf<String, String>()

        for (i in optionsHeader.indices) {
            options[optionsHeader[i]] = optionsRecord[i]
        }

        val itemsHeader = data[2].split(",")

        val items = mutableListOf<Map<String, String>>()

        for (i in 3 until data.size - 1) {
            val item = mutableMapOf<String, String>()

            val itemsRecord = data[i].split(",")

            for (j in itemsHeader.indices) {
                item[itemsHeader[j]] = itemsRecord[j].substringAfter("\"").substringBeforeLast("\"")
            }

            items.add(item)
        }

        Storable("", options, items)
    }
    StorageType.TXT -> throw IllegalArgumentException("Storage type $storageType not supported")
}