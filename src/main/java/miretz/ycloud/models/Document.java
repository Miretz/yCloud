package miretz.ycloud.models;

public class Document {

	private final String fileName;
	private final String comment;
	private final String creator;
	private final long size;
	private final String mimeType;

	public Document(final String fileName, final long size, final String mimeType, final String comment, final String creator) {
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
