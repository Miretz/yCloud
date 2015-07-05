package miretz.ycloud.services

import java.io.File
import java.io.IOException
import java.io.InputStream

import miretz.ycloud.models.Document

import com.vaadin.server.FileResource

public interface DocumentService {

    public fun deleteFile(document: Document): Boolean

    public fun deleteAllFiles()

    public fun getModifiedDate(document: Document): String

    public fun getFileResource(document: Document): FileResource

    public fun getThumbnailFileResource(document: Document): FileResource?

    public fun getFreeSpace(): Double

    public fun getSizeOfFiles(): Double

    public fun getSizeInMbDouble(size: Long): Double

    public fun saveThumbnail(document: Document)

    public fun getAllFilesZip(documents: List<Document>): InputStream

    public fun getFile(document: Document): File

    public fun getFileMimeType(document: Document): String

}