package io.sunland.chainpass.common

import java.nio.file.Files
import java.nio.file.Path
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.io.path.writeText

actual class Storage actual constructor(
    actual val dirPath: String,
    actual val options: StorageOptions
) {
    actual fun store(storable: Storable): String {
        val storePath: Path = Path.of("$dirPath/store")

        if (!Files.exists(storePath)) {
            Files.createDirectory(storePath)
        }

        val date = (DateFormat.getDateTimeInstance() as SimpleDateFormat).apply {
            applyPattern("yyyy.MM.dd-HH.mm.ss")
        }.format(Date())

        val fileName = "${storable.keys.first().values.toSet().joinToString("-")}_$date"

        val filePath = when (options.type) {
            StorageType.JSON -> Path.of("$dirPath/store/$fileName.json")
            StorageType.CSV -> Path.of("$dirPath/store/$fileName.csv")
            StorageType.TXT -> Path.of("$dirPath/store/$fileName.txt")
        }

        if (!Files.exists(filePath)) {
            Files.createFile(filePath)
        }

        filePath.writeText(storable.toString(options.type))

        return fileName
    }
}