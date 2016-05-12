package miretz.ycloud.views.windows

import com.vaadin.ui.*
import miretz.ycloud.models.Document
import miretz.ycloud.services.DocumentService

class LightboxWindow(protected var documentService: DocumentService) : Window("Image Preview") {

    init {
        center()
    }

    fun setImage(document: Document) {

        caption = "Image Preview: " + document.fileName

        val layout = HorizontalLayout()
        content = layout

        layout.setSizeFull()
        layout.isImmediate = true

        val image = Image(null, documentService.getFileResource(document))
        layout.addComponent(image)
        layout.setComponentAlignment(image, Alignment.MIDDLE_CENTER)

        image.setHeight("75%")
        image.addStyleName("cursor-pointer")
        image.addClickListener { close() }

        setSizeFull()
        isImmediate = true

    }

    fun setVideo(document: Document){

        caption = "Video Preview: " + document.fileName

        val layout = HorizontalLayout()
        content = layout

        layout.setSizeFull()
        layout.isImmediate = true

        val video = Video()
        video.isAutoplay = true
        video.isHtmlContentAllowed = true
        video.setSource(documentService.getFileResource(document))

        layout.addComponent(video)
        layout.setComponentAlignment(video, Alignment.MIDDLE_CENTER)

        video.setHeight("75%")
        video.addStyleName("cursor-pointer")

        setSizeFull()
        isImmediate = true
    }
}
