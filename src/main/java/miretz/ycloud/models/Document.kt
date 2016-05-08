package miretz.ycloud.models

import java.util.*

class Document(val contentId: String, val fileName: String, val parentId: String, val metadata: Map<String, String>, val type: String, val retentionDate: Date?) {
    companion object {
        val TYPE_FILE: String = "file"
        val TYPE_FOLDER: String = "folder"
    }

}

enum class DocumentMetadata private constructor(private val text: String) {

    COMMENT("comment"),
    CREATOR("creator");

    override fun toString(): String {
        return text
    }

}


