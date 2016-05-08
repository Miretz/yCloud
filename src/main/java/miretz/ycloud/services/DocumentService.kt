package miretz.ycloud.services

import com.vaadin.server.FileResource
import miretz.ycloud.models.Document
import java.io.File
import java.io.InputStream

interface DocumentService {

    fun deleteFile(document: Document): Boolean

    fun deleteAllFiles()

    fun getModifiedDate(document: Document): String

    fun getFileResource(document: Document): FileResource

    fun getThumbnailFileResource(document: Document): FileResource?

    fun getFreeSpace(): String

    fun getSizeOfFiles(): String

    fun saveThumbnail(document: Document)

    fun getAllFilesZip(documents: List<Document>): InputStream

    fun getFile(document: Document): File

    fun getFileMimeType(document: Document): String

}