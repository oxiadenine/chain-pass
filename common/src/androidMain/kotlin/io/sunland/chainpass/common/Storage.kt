package io.sunland.chainpass.common

import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

actual class Storage actual constructor(
    actual val dirPath: String,
    actual val type: StorageType
) {
    actual fun store(storable: Storable): String {
        val storePath = "$dirPath/store"

        if (!File(storePath).exists()) {
            File(storePath).mkdir()
        }

        val date = (DateFormat.getDateTimeInstance() as SimpleDateFormat).apply {
            applyPattern("yyyy.MM.dd-HH.mm.ss")
        }.format(Date())

        val fileName = "${storable.keys.first().values.toSet().joinToString("-")}_$date"

        val filePath = when (type) {
            StorageType.JSON -> "$dirPath/store/$fileName.json"
            StorageType.CSV -> "$dirPath/store/$fileName.csv"
            StorageType.TXT -> "$dirPath/store/$fileName.txt"
        }

        if (!File(filePath).exists()) {
            File(filePath).createNewFile()
        }

        File(filePath).writeText(storable.toString(type))

        return fileName
    }
}