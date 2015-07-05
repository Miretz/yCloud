package miretz.ycloud.views.windows

import miretz.ycloud.models.Document
import miretz.ycloud.services.DocumentService

import com.vaadin.event.MouseEvents
import com.vaadin.event.MouseEvents.ClickEvent
import com.vaadin.ui.Alignment
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.Image
import com.vaadin.ui.Window

public class LightboxWindow(protected var documentService: DocumentService) : Window("Image Preview") {

    init {
        center()
    }

    public fun setImage(document: Document) {

        setCaption("Image Preview: " + document.fileName)

        val layout = HorizontalLayout()
        setContent(layout)

        layout.setSizeFull()
        layout.setImmediate(true)

        val image = Image(null, documentService.getFileResource(document))
        layout.addComponent(image)
        layout.setComponentAlignment(image, Alignment.MIDDLE_CENTER)

        image.setHeight("75%")
        image.addStyleName("cursor-pointer")
        image.addClickListener(object : MouseEvents.ClickListener {

            override fun click(event: ClickEvent) {
                close()
            }
        })

        setSizeFull()
        setImmediate(true)

    }
}
