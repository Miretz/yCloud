package miretz.ycloud.views.windows

import com.vaadin.ui.*
import miretz.ycloud.models.Document
import miretz.ycloud.services.DatabaseService
import java.util.*

class CreateFolderWindow(databaseService: DatabaseService, currentFolder: Document) : Window("Create Folder") {

    init {

        val fileNameLabel = Label("Folder Name:")
        val fileNameField = TextField()
        val commentLabel = Label("Folder Comment:")
        val commentField = TextField()

        center()

        // Some basic content for the window
        val content = VerticalLayout()
        content.setMargin(true)
        content.isSpacing = true

        setContent(content)
        content.setWidth("310px")

        // Disable the close button
        isClosable = true
        isResizable = false
        isModal = true

        fileNameLabel.isVisible = true
        fileNameField.isVisible = true
        fileNameField.setWidth("250px")

        content.addComponent(fileNameLabel)
        content.addComponent(fileNameField)

        commentLabel.isVisible = true
        commentField.isVisible = true
        commentField.setWidth("250px")

        content.addComponent(commentLabel)
        content.addComponent(commentField)

        val btnCreateUser = Button("Create Folder")
        btnCreateUser.addClickListener {
            val creator = (session.getAttribute("user")) as String

            val metadata = HashMap<String, String>()
            metadata.put("creator", creator)
            metadata.put("comment", commentField.value)

            val document = Document(UUID.randomUUID().toString(), fileNameField.value, currentFolder.contentId, metadata, Document.TYPE_FOLDER, null)

            databaseService.addDocument(document)

            close()
        }

        content.addComponent(btnCreateUser)

    }
}
