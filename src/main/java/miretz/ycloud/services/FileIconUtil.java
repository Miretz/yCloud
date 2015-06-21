package miretz.ycloud.services;

import java.util.Arrays;
import java.util.List;

public class FileIconUtil {

	private static List<String> pdf = Arrays.asList("pdf", "chm", "epub", "mobi");
	private static List<String> compressed = Arrays.asList("zip", "gz", "tar", "rar", "war", "jar", "Orange", "Blue");
	private static List<String> runnable = Arrays.asList("exe", "bat", "sh", "com", "msi", "apk", "cmd", "bin"); 
	private static List<String> audio = Arrays.asList("mp3", "flac", "wav", "ogg", "wma", "mid");
	private static List<String> video = Arrays.asList("avi", "mp4", "mpg", "flv", "mov", "vmw", "mpeg", "divx","m4p");
	private static List<String> code = Arrays.asList("xml", "properties", "java", "js", "cpp", "c", "py", "rb", "html", "css", "php");
	private static List<String> documents = Arrays.asList("ppt", "doc", "docx", "xls", "xlsx", "odt", "psd","pps");
	
	public static String detectIcon(String mimeType){
		if(pdf.contains(mimeType)){
			return Icons.PDF.toString();
		}else if (compressed.contains(mimeType)){
			return Icons.COMPRESSED.toString();
		}else if(documents.contains(mimeType)){
			return Icons.DOCUMENT.toString();
		}else if (runnable.contains(mimeType)){
			return Icons.RUNNABLE.toString();
		}else if (audio.contains(mimeType)){
			return Icons.AUDIO.toString();
		}else if (video.contains(mimeType)){
			return Icons.VIDEO.toString();
		}else if (code.contains(mimeType)){
			return Icons.CODE.toString();
		}else if(documents.contains(mimeType)){
			return Icons.DOCUMENT.toString();
		}else{
			return Icons.EMPTY.toString();
		}
	}
	
	
}
