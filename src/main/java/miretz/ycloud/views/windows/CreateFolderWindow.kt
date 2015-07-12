package miretz.ycloud.views.windows

import java.util.HashMap
import java.util.UUID

import miretz.ycloud.models.Document
import miretz.ycloud.services.DatabaseService
import miretz.ycloud.services.DocumentService

import com.vaadin.ui.Button
import com.vaadin.ui.Button.ClickEvent
import com.vaadin.ui.Label
import com.vaadin.ui.TextField
import com.vaadin.ui.VerticalLayout
import com.vaadin.ui.Window

public class CreateFolderWindow(databaseService: DatabaseService, currentFolder: Document) : Window("Create Folder") {

    init {

        val fileNameLabel = Label("Folder Name:")
        val fileNameField = TextField()
        val commentLabel = Label("Folder Comment:")
        val commentField = TextField()

        center()

        // Some basic content for the window
        val content = VerticalLayout()
        content.setMargin(true)
        content.setSpacing(true)

        setContent(content)
        content.setWidth("310px")

        // Disable the close button
        setClosable(true)
        setResizable(false)
        setModal(true)

        fileNameLabel.setVisible(true)
        fileNameField.setVisible(true)
        fileNameField.setWidth("250px")

        content.addComponent(fileNameLabel)
        content.addComponent(fileNameField)

        commentLabel.setVisible(true)
        commentField.setVisible(true)
        commentField.setWidth("250px")

        content.addComponent(commentLabel)
        content.addComponent(commentField)

        val btnCreateUser = Button("Create Folder")
        btnCreateUser.addClickListener(object : Button.ClickListener {

            override fun buttonClick(event: ClickEvent) {

                val creator = (getSession().getAttribute("user")) as String

                val metadata = HashMap<String, String>()
                metadata.put("creator", creator)
                metadata.put("comment", commentField.getValue())

                val document = Document(UUID.randomUUID().toString(), fileNameField.getValue(), currentFolder.contentId, metadata, Document.TYPE_FOLDER)

                databaseService.addDocument(document)

                close()

            }
        })

        content.addComponent(btnCreateUser)

    }
}
