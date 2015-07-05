package miretz.ycloud.views.windows


import miretz.ycloud.models.Document
import miretz.ycloud.services.DatabaseService
import miretz.ycloud.services.DocumentService

import com.vaadin.ui.Alignment
import com.vaadin.ui.Button
import com.vaadin.ui.Button.ClickEvent
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.Label
import com.vaadin.ui.Notification
import com.vaadin.ui.VerticalLayout
import com.vaadin.ui.Window

SuppressWarnings("serial")
public class ConfirmationWindow(documents: List<Document>, documentService: DocumentService, databaseService: DatabaseService) : Window("User confirmation required") {

    init {

        center()

        val filesToDelete = filenamesToString(documents)
        val text = "Files to delete: \n" + filesToDelete

        // Some basic content for the window
        val content = VerticalLayout()
        content.setMargin(true)
        setContent(content)

        // label
        val textLabel = Label(text)
        content.addComponent(textLabel)
        content.setComponentAlignment(textLabel, Alignment.MIDDLE_CENTER)

        // buttons
        val buttons = HorizontalLayout()

        val yes = Button("Yes", object : Button.ClickListener {
            override fun buttonClick(event: ClickEvent) {
                for (document in documents) {
                    documentService.deleteFile(document)
                    databaseService.deleteDocument(document.contentId)
                }
                Notification.show("Files Deleted:", filesToDelete, Notification.Type.HUMANIZED_MESSAGE)
                close()
            }
        })
        val no = Button("No", object : Button.ClickListener {
            override fun buttonClick(event: ClickEvent) {
                close()
            }
        })

        buttons.addComponent(yes)
        buttons.addComponent(no)
        content.addComponent(buttons)
        content.setComponentAlignment(buttons, Alignment.MIDDLE_CENTER)

    }

    private fun filenamesToString(documents: List<Document>): String {
        val sb = StringBuilder()
        for (doc in documents) {
            sb.append(doc.fileName)
            sb.append("\n")
        }
        return sb.toString()
    }

}
