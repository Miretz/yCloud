package miretz.ycloud.models;

import java.util.Map;

public class Document {

	public static final String TYPE_FILE = "file";
	public static final String TYPE_FOLDER = "folder";

	private final String contentId;
	private final String fileName;
	private final String parentId;
	private final Map<String, String> metadata;
	private final String type;

	public Document(String contentId, String fileName, String parentId, Map<String, String> metadata, String type) {
		super();
		this.contentId = contentId;
		this.fileName = fileName;
		this.parentId = parentId;
		this.metadata = metadata;
		this.type = type;
	}

	public String getContentId() {
		return contentId;
	}

	public String getFileName() {
		return fileName;
	}

	public String getParentId() {
		return parentId;
	}

	public Map<String, String> getMetadata() {
		return metadata;
	}

	public String getType() {
		return type;
	}

}
