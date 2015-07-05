package miretz.ycloud.services.utils

import java.util.Arrays

public object FileIconUtil {

    private val pdf = Arrays.asList("pdf", "chm", "epub", "mobi")
    private val compressed = Arrays.asList("zip", "gz", "tar", "rar", "war", "jar", "Orange", "Blue")
    private val runnable = Arrays.asList("exe", "bat", "sh", "com", "msi", "apk", "cmd", "bin")
    private val audio = Arrays.asList("mp3", "flac", "wav", "ogg", "wma", "mid")
    private val video = Arrays.asList("avi", "mp4", "mpg", "flv", "mov", "vmw", "mpeg", "divx", "m4p")
    private val code = Arrays.asList("xml", "properties", "java", "js", "cpp", "c", "py", "rb", "html", "css", "php")
    private val documents = Arrays.asList("ppt", "doc", "docx", "xls", "xlsx", "odt", "psd", "pps")

    public fun detectIcon(mimeType: String): String {
        if (pdf.contains(mimeType)) {
            return Icons.PDF.toString()
        } else if (compressed.contains(mimeType)) {
            return Icons.COMPRESSED.toString()
        } else if (documents.contains(mimeType)) {
            return Icons.DOCUMENT.toString()
        } else if (runnable.contains(mimeType)) {
            return Icons.RUNNABLE.toString()
        } else if (audio.contains(mimeType)) {
            return Icons.AUDIO.toString()
        } else if (video.contains(mimeType)) {
            return Icons.VIDEO.toString()
        } else if (code.contains(mimeType)) {
            return Icons.CODE.toString()
        } else if (documents.contains(mimeType)) {
            return Icons.DOCUMENT.toString()
        } else {
            return Icons.EMPTY.toString()
        }
    }


}
