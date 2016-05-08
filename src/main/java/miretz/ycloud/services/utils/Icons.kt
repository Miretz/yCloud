package miretz.ycloud.services.utils

enum class Icons private constructor(private val text: String) {

    AUDIO("img/icons/audio.png"),
    CODE("img/icons/code.png"),
    COMPRESSED("img/icons/compressed.png"),
    DOCUMENT("img/icons/document.png"),
    PDF("img/icons/pdf.png"),
    RUNNABLE("img/icons/runnable.png"),
    VIDEO("img/icons/video.png"),
    EMPTY("img/icons/empty.png"),
    FOLDER("img/icons/folder.png");

    override fun toString(): String {
        return text
    }
}
