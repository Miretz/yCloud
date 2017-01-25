package miretz.ycloud.services

import java.awt.Dimension
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Arrays
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import javax.imageio.ImageIO
import javax.inject.Singleton

import miretz.ycloud.models.Document
import miretz.ycloud.services.utils.DirectoryFilenameFilter

import org.apache.log4j.Logger

import com.google.inject.Inject
import com.google.inject.name.Named
import com.vaadin.server.FileResource
import miretz.ycloud.services.utils.CustomFileNameResource
import org.apache.commons.io.FileUtils
import java.io.File.separator

@Singleton
class FileSystemService
@Inject
constructor(@Named("thumbnailDir") thumbnailDir: String, @Named("uploadDir") uploadDir: String, @Named("dateFormat") protected val dateFormat: String, protected val databaseService: DatabaseService) : DocumentService {

    protected val thumbnailDimensions: Dimension
    protected val thumbnailDir: File
    protected val uploadDir: File

    init {
        this.thumbnailDimensions = Dimension(50, 50)

        val fixedUploadDir = fixDirPath(uploadDir)
        val fixedThumbDir = fixDirPath(thumbnailDir)

        val uploadDirFile: File? = File(fixedUploadDir)
        val thumbnailDirFile: File? = File(fixedThumbDir)

        this.uploadDir = uploadDirFile ?: throw IllegalStateException("upload directory is not set")
        this.thumbnailDir = thumbnailDirFile ?: throw IllegalStateException("thumbnail directory is not set")

    }

    override fun deleteFile(document: Document): Boolean {

        val contentId = document.contentId

        //delete descendants
        databaseService.getDescendants(contentId).forEach {
            f ->
            deleteFile(f)
        }

        // delete thumbnail
        val thumbnail = getFileFromDir(thumbnailDir, contentId)
        thumbnail?.delete()

        // delete file
        val file = getFileFromDir(uploadDir, contentId)
        if (file != null && file.exists()) {
            return file.delete()
        }

        return false

    }

    override fun deleteAllFiles() {

        if (logger.isDebugEnabled) {
            logger.debug("Delete all files started!")
        }

        for (file in uploadDir.listFiles()) {
            file.delete()
        }
        for (file in thumbnailDir.listFiles()) {
            file.delete()
        }

    }

    override fun getModifiedDate(document: Document): String {
        val file = getFileFromDir(uploadDir, document.contentId)
        val sdf = SimpleDateFormat(dateFormat)
        val timestamp = file?.lastModified()
        return sdf.format(timestamp)
    }

    override fun getRetentionDate(document: Document): String {
        val timestamp = document.retentionDate ?: return ""
        val sdf = SimpleDateFormat(dateFormat)
        return sdf.format(timestamp)
    }

    override fun getFileResource(document: Document): FileResource {
        val file = getFileFromDir(uploadDir, document.contentId) ?: throw IllegalStateException("file not found")
        return CustomFileNameResource(file, document.fileName)
    }

    override fun getThumbnailFileResource(document: Document): FileResource? {
        val thumbnail = getFileFromDir(thumbnailDir, document.contentId) ?: return null
        return FileResource(thumbnail)
    }

    override fun getFreeSpace(): String {
        val freeSpace = uploadDir.freeSpace
        return FileUtils.byteCountToDisplaySize(freeSpace)
    }

    override fun getSizeOfFiles(): String {
        val sizeOfDir = FileUtils.sizeOfDirectory(uploadDir)
        return FileUtils.byteCountToDisplaySize(sizeOfDir)
    }

    override fun saveThumbnail(document: Document) {

        val imgFile = getFileFromDir(uploadDir, document.contentId)
        val imgExtension = getFileMimeType(document)
        if (Arrays.asList(*IMAGE_FORMATS).contains(imgExtension)) {

            val sourceImage = ImageIO.read(imgFile)

            var xPos = 0
            var yPos = 0

            var scaled: Image?
            var img: BufferedImage?

            if (sourceImage.width > sourceImage.height) {
                scaled = sourceImage.getScaledInstance(-1, thumbnailDimensions.height, Image.SCALE_SMOOTH)
                img = BufferedImage(scaled!!.getWidth(null), thumbnailDimensions.height, BufferedImage.TYPE_INT_RGB)
                xPos = ((img.width / 2) - (thumbnailDimensions.getWidth() / 2)).toInt()
            } else {
                scaled = sourceImage.getScaledInstance(thumbnailDimensions.width, -1, Image.SCALE_SMOOTH)
                img = BufferedImage(thumbnailDimensions.width, scaled!!.getHeight(null), BufferedImage.TYPE_INT_RGB)
                yPos = ((img.height / 2) - (thumbnailDimensions.getHeight() / 2)).toInt()
            }

            img.createGraphics().drawImage(scaled, 0, 0, null)
            val cropped = img.getSubimage(xPos, yPos, thumbnailDimensions.width, thumbnailDimensions.height)
            ImageIO.write(cropped, imgExtension, File(thumbnailDir.absolutePath + separator + document.contentId))
        }

    }

    override fun getAllFilesZip(documents: List<Document>): InputStream {
        try {
            val f = File(uploadDir.absolutePath + separator + "all_files.zip")
            if (f.exists() && f.isFile) {
                f.delete()
            }

            val files = ArrayList<File>()
            for (doc in documents) {
                val file = getFileFromDir(uploadDir, doc.contentId)
                if (file != null) {
                    files.add(file)
                }
            }

            val out = ZipOutputStream(FileOutputStream(f))
            for (file in files) {
                if (file.isFile) {
                    val e = ZipEntry(file.name)
                    out.putNextEntry(e)
                    val data = ByteArray(BUFFER)
                    val fileInputStream = FileInputStream(file)
                    val bufferedInputStream = BufferedInputStream(fileInputStream, BUFFER)
                    var size = 0
                    while (size != -1) {
                        size = bufferedInputStream.read(data, 0, BUFFER)
                        out.write(data, 0, size)
                    }
                    bufferedInputStream.close()
                    out.closeEntry()
                }
            }
            out.close()

            return FileInputStream(f)
        } catch (e: FileNotFoundException) {
            throw IllegalStateException("Zip file not found!", e)
        } catch (e: IOException) {
            throw IllegalStateException("Failed to create Zip file!", e)
        }

    }

    private fun getFileFromDir(dir: File, contentId: String): File? {
        var file: File? = null
        val files = dir.listFiles(DirectoryFilenameFilter(contentId))
        if (files.size == 1) {
            file = files[0]
        }
        return file
    }

    private fun fixDirPath(dir: String): String {
        return if (dir.endsWith(separator)) dir else dir + separator
    }

    override fun getFile(document: Document): File {
        return getFileFromDir(uploadDir, document.contentId) ?: throw IllegalStateException("file not found")
    }

    override fun getFileMimeType(document: Document): String {
        val name = document.fileName
        val lastIndexOf = name.lastIndexOf(".")
        if (lastIndexOf == -1) {
            return "" // empty extension
        }
        return name.substring(lastIndexOf + 1)
    }

    companion object {

        private val logger = Logger.getLogger(FileSystemService::class.java.simpleName)

        protected val IMAGE_FORMATS: Array<String> = arrayOf("jpg", "png", "bmp", "gif")
        protected val BUFFER: Int = 2048
    }

}
