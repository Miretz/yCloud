package miretz.ycloud.models;

public class Document {

	private String fileName;
	private String comment;
	private String creator;
	private long size;
	private String mimeType;

	public Document(String fileName, long size, String mimeType, String comment, String creator) {
		this.fileName = fileName;
		this.size = size;
		this.mimeType = mimeType;
		this.comment = comment;
		this.creator = creator;
	}

	public String getCreator() {
		return creator;
	}

	public String getFileName() {
		return fileName;
	}

	public long getSize() {
		return size;
	}
	
	public String getMimeType() {
		return mimeType;
	}

	public String getComment() {
		return comment;
	}

	
	
}
