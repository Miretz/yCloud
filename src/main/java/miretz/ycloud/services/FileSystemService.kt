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

Singleton
public class FileSystemService
@Inject
constructor(Named("thumbnailDir") thumbnailDir: String, Named("uploadDir") uploadDir: String, Named("dateFormat") protected val dateFormat: String) : DocumentService {
    protected val thumbnailDimensions: Dimension

    protected val thumbnailDir: File
    protected val uploadDir: File

    init {
        this.thumbnailDimensions = Dimension(50, 50)

        val uploadDirFile : File? = File(uploadDir)
        val thumbnailDirFile : File? = File(thumbnailDir)

		this.uploadDir = uploadDirFile ?: throw IllegalStateException("upload directory is not set")
		this.thumbnailDir =  thumbnailDirFile ?: throw IllegalStateException("thumbnail directory is not set")

    }

    override fun deleteFile(document: Document): Boolean {

        val contentId = document.contentId

        // delete thumbnail
        val thumbnail = getFileFromDir(thumbnailDir, contentId)
        thumbnail?.delete()

        // delete file
        val file = getFileFromDir(uploadDir, contentId)
        if (file != null) {
            return file.delete()
        }

        return false

    }

    override fun deleteAllFiles() {

        if (logger.isDebugEnabled()) {
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

    override fun getFileResource(document: Document): FileResource {
        return CustomFileNameResource(getFileFromDir(uploadDir, document.contentId), document.fileName)
    }

    override fun getThumbnailFileResource(document: Document): FileResource? {
        val thumbnail = getFileFromDir(thumbnailDir, document.contentId) ?: return null
        return FileResource(thumbnail)
    }

    override fun getFreeSpace(): Double {
        val freeSpace = uploadDir.getFreeSpace()
        return getSizeInMbDouble(freeSpace)
    }

    override fun getSizeOfFiles(): Double {
        var fileSizes = 0L
        for (file in uploadDir.listFiles()!!) {
            fileSizes += file.length()
        }
        return getSizeInMbDouble(fileSizes)
    }

    override fun getSizeInMbDouble(size: Long): Double {
        val sizeMb = size.toDouble() / 1024.0 / 1024.0
        return Math.round(sizeMb * 100.0) / 100.0
    }

    override fun saveThumbnail(document: Document) {

        val imgFile = getFileFromDir(uploadDir, document.contentId)
        val imgExtension = getFileMimeType(document)
        if (Arrays.asList(*IMAGE_FORMATS).contains(imgExtension)) {

            val sourceImage = ImageIO.read(imgFile)

            var xPos = 0
            var yPos = 0

            var scaled: Image? = null
            var img: BufferedImage? = null

            if (sourceImage.getWidth() > sourceImage.getHeight()) {
                scaled = sourceImage.getScaledInstance(-1, thumbnailDimensions.height, Image.SCALE_SMOOTH)
                img = BufferedImage(scaled!!.getWidth(null), thumbnailDimensions.height, BufferedImage.TYPE_INT_RGB)
                xPos = ((img.getWidth() / 2) - (thumbnailDimensions.getWidth() / 2)).toInt()
            } else {
                scaled = sourceImage.getScaledInstance(thumbnailDimensions.width, -1, Image.SCALE_SMOOTH)
                img = BufferedImage(thumbnailDimensions.width, scaled!!.getHeight(null), BufferedImage.TYPE_INT_RGB)
                yPos = ((img.getHeight() / 2) - (thumbnailDimensions.getHeight() / 2)).toInt()
            }

            img.createGraphics().drawImage(scaled, 0, 0, null)
            val cropped = img.getSubimage(xPos, yPos, thumbnailDimensions.width, thumbnailDimensions.height)
            ImageIO.write(cropped, imgExtension, File(thumbnailDir.getAbsolutePath() + File.separator + document.contentId))
        }

    }

    override public fun getAllFilesZip(documents: List<Document>): InputStream {
        try {
            val f = File(uploadDir.getAbsolutePath() + File.separator + "all_files.zip")
            if (f.exists() && f.isFile()) {
                f.delete()
            }

            val files = ArrayList<File>()
            for (doc in documents) {
                files.add(getFileFromDir(uploadDir, doc.contentId))
            }

            val out = ZipOutputStream(FileOutputStream(f))
            for (file in files) {
                if (file.isFile()) {
                    val e = ZipEntry(file.getName())
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
        if (files.size() == 1) {
            file = files[0]
        }
        return file
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

        private val logger = Logger.getLogger(javaClass<FileSystemService>().getName())

        protected val IMAGE_FORMATS: Array<String> = arrayOf("jpg", "png", "bmp", "gif")
        protected val BUFFER: Int = 2048
    }

}