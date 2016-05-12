package miretz.ycloud.models

enum class DocumentMetadata private constructor(private val text: String) {

    COMMENT("comment"),
    CREATOR("creator");

    override fun toString(): String {
        return text
    }

}