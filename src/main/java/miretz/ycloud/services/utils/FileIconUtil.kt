package miretz.ycloud.services.utils

import java.util.*

object FileIconUtil {

    private val pdf = Arrays.asList("pdf", "chm", "epub", "mobi")
    private val compressed = Arrays.asList("zip", "gz", "tar", "rar", "war", "jar", "Orange", "Blue")
    private val runnable = Arrays.asList("exe", "bat", "sh", "com", "msi", "apk", "cmd", "bin")
    private val audio = Arrays.asList("mp3", "flac", "wav", "ogg", "wma", "mid")
    private val video = Arrays.asList("avi", "mp4", "mpg", "flv", "mov", "vmw", "mpeg", "divx", "m4p")
    private val code = Arrays.asList("xml", "properties", "java", "js", "cpp", "c", "py", "rb", "html", "css", "php")
    private val documents = Arrays.asList("ppt", "doc", "docx", "xls", "xlsx", "odt", "psd", "pps")

    fun detectIcon(mimeType: String): Icons {
        when {
            pdf.contains(mimeType) -> return Icons.PDF
            compressed.contains(mimeType) -> return Icons.COMPRESSED
            documents.contains(mimeType) -> return Icons.DOCUMENT
            runnable.contains(mimeType) -> return Icons.RUNNABLE
            audio.contains(mimeType) -> return Icons.AUDIO
            video.contains(mimeType) -> return Icons.VIDEO
            code.contains(mimeType) -> return Icons.CODE
            documents.contains(mimeType) -> return Icons.DOCUMENT
            else -> return Icons.EMPTY
        }
    }


}
