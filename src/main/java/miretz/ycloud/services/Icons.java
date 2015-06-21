package miretz.ycloud.services;

public enum Icons {

	AUDIO("img/icons/audio.png"),
	CODE("img/icons/code.png"),
	COMPRESSED("img/icons/compressed.png"),
	DOCUMENT("img/icons/document.png"),
	PDF("img/icons/pdf.png"),
	RUNNABLE("img/icons/runnable.png"),
	VIDEO("img/icons/video.png"),
	EMPTY("img/icons/empty.png"),
    ;

    private final String text;

    /**
     * @param text
     */
    private Icons(final String text) {
        this.text = text;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return text;
    }
}
