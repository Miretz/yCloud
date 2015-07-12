package miretz.ycloud.models

public class Document(public val contentId: String, public val fileName: String, public val parentId: String, public val metadata: Map<String, String>, public val type: String) {

    companion object {
        public val TYPE_FILE: String = "file"
        public val TYPE_FOLDER: String = "folder"
    }

}
