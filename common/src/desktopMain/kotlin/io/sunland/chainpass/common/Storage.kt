package io.sunland.chainpass.common

import java.nio.file.Files
import java.nio.file.Path
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.io.path.extension
import kotlin.io.path.readText
import kotlin.io.path.writeText

actual class Storage actual constructor(actual val dirPath: String) {
    actual fun store(storable: Storable, storageType: StorageType): String {
        val storePath: Path = Path.of("$dirPath/store")

        if (!Files.exists(storePath)) {
            Files.createDirectory(storePath)
        }

        val date = (DateFormat.getDateTimeInstance() as SimpleDateFormat).apply {
            applyPattern("yyyy.MM.dd-HH.mm.ss")
        }.format(Date())

        val fileName = "${storable.chain["name"]}_$date"

        val filePath = when (storageType) {
            StorageType.JSON -> Path.of("$dirPath/store/$fileName.json")
            StorageType.CSV -> Path.of("$dirPath/store/$fileName.csv")
            StorageType.TXT -> Path.of("$dirPath/store/$fileName.txt")
        }

        if (!Files.exists(filePath)) {
            Files.createFile(filePath)
        }

        filePath.writeText(storable.toString(storageType))

        return fileName
    }

    actual fun unstore(filePath: String): Storable {
        if (!Files.exists(Path.of(filePath))) {
            error("File $filePath does not exists")
        }

        val storageType = StorageType.valueOf(Path.of(filePath).extension.uppercase())

        return Path.of(filePath).readText().toStorable(storageType)
    }
}