package io.sunland.chainpass.common

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

expect class Storage(dirPath: String, options: StorageOptions) {
    val dirPath: String
    val options: StorageOptions

    fun store(storable: Storable): String
}

enum class StorageType { JSON, CSV, TXT }

data class StorageOptions(val isPrivate: Boolean = true, val type: StorageType = StorageType.JSON)

typealias Storable = Map<Map<String, String>, List<Map<String, String>>>

fun Storable.toString(storageType: StorageType) = when (storageType) {
    StorageType.JSON -> JsonArray(this.values.flatMap { value ->
        value.map {
            JsonObject(it.map { entry ->
                entry.key to JsonPrimitive(entry.value)
            }.toMap())
        }
    }).toString()
    StorageType.CSV -> buildString {
        val data = this@toString.values.flatten()

        val header = data.flatMap { it.keys }.toSet().joinToString(",")

        append("$header\n")

        val records = data.map {
            it.values.joinToString(",") { field ->
                "\"${field.replace("\"", "\"\"")}\""
            }
        }

        records.forEach { record -> append("$record\n") }
    }
    StorageType.TXT -> buildString { append(this@toString.values.flatten()) }
}