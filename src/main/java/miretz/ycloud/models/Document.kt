package miretz.ycloud.models

class Document(val contentId: String, val fileName: String, val parentId: String, val metadata: Map<String, String>, val type: String) {
    companion object {
        val TYPE_FILE: String = "file"
        val TYPE_FOLDER: String = "folder"
    }
}
