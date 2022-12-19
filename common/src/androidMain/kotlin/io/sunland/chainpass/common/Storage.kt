package io.sunland.chainpass.common

import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

actual class Storage actual constructor(actual val dirPath: String) {
    actual fun store(storable: Storable, storageType: StorageType): String {
        val storePath = "$dirPath/store"

        if (!File(storePath).exists()) {
            File(storePath).mkdir()
        }

        val date = (DateFormat.getDateTimeInstance() as SimpleDateFormat).apply {
            applyPattern("yyyy.MM.dd-HH.mm.ss")
        }.format(Date())

        val fileName = "${storable.chain["name"]}_$date"

        val filePath = when (storageType) {
            StorageType.JSON -> "$dirPath/store/$fileName.json"
            StorageType.CSV -> "$dirPath/store/$fileName.csv"
            StorageType.TXT -> "$dirPath/store/$fileName.txt"
        }

        if (!File(filePath).exists()) {
            File(filePath).createNewFile()
        }

        File(filePath).writeText(storable.toString(storageType))

        return fileName
    }

    actual fun unstore(filePath: String): Storable {
        if (!File(filePath).exists()) {
            error("File $filePath does not exists")
        }

        val storageType = StorageType.valueOf(File(filePath).extension.uppercase())

        return File(filePath).readText().toStorable(storageType)
    }
}