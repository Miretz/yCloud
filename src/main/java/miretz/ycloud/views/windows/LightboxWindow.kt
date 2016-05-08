package miretz.ycloud.views.windows

import com.vaadin.ui.Alignment
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.Image
import com.vaadin.ui.Window
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
}
