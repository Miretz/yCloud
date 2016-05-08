package miretz.ycloud.views.windows

import com.vaadin.server.Page
import com.vaadin.ui.*
import com.vaadin.ui.Upload.*
import miretz.ycloud.models.Document
import miretz.ycloud.services.DatabaseService
import miretz.ycloud.services.DocumentService
import org.apache.log4j.Logger
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.file.Paths
import java.util.*

class UploadWindow(documentService: DocumentService, databaseService: DatabaseService, uploadDir: String, currentFolder: Document) : Window("Upload File") {

    private val upload: Upload
    private val bar = ProgressBar()
    private val uploadCaption = Label("Uploading in progress. Please wait...")
    private val filenameLabel = Label("File Name:")
    private val filenameField = TextField()
    private val commentLabel = Label("File comment:")
    private val commentField = TextField()
    private val uploadButton = Button("Upload file")

    init {
        center()

        // Some basic content for the window
        val content = VerticalLayout()
        content.setMargin(true)
        content.isSpacing = true
        setContent(content)
        content.setWidth("310px")

        isClosable = true
        isResizable = false
        isModal = true

        bar.setWidth("250px")
        val progressLayout = VerticalLayout()
        progressLayout.addComponent(bar)
        progressLayout.setComponentAlignment(bar, Alignment.MIDDLE_CENTER)
        progressLayout.addComponent(uploadCaption)
        progressLayout.isVisible = false
        progressLayout.isSpacing = true
        progressLayout.setWidth("100%")
        content.addComponent(progressLayout)
        content.setComponentAlignment(progressLayout, Alignment.MIDDLE_CENTER)

        class FileUploader : Receiver, SucceededListener {
            var file: File? = null
            var uid: String = ""

            override fun receiveUpload(filenameV: String?, mimeType: String): OutputStream {
                try {
                    uid = UUID.randomUUID().toString()
                    file = File(uploadDir + uid)
                } catch (e: java.io.FileNotFoundException) {
                    Notification("Could not open file", e.message, Notification.Type.ERROR_MESSAGE).show(Page.getCurrent())
                    logger.error("Could not open file!", e)
                    close()
                }

                return FileOutputStream(file)
            }

            override fun uploadSucceeded(event: SucceededEvent) {
                UI.getCurrent().pollInterval = -1

                val creator = (session.getAttribute("user")) as String

                val metadata = HashMap<String, String>()
                metadata.put("creator", creator)
                metadata.put("comment", commentField.value)

                val document = Document(uid, filenameField.value, currentFolder.contentId, metadata, Document.TYPE_FILE)

                documentService.saveThumbnail(document)

                databaseService.addDocument(document)

                Notification.show("Upload completed:", event.filename, Notification.Type.HUMANIZED_MESSAGE)
                close()
            }
        }

        val receiver = FileUploader()
        upload = Upload("Please select your file:", receiver)
        upload.addSucceededListener(receiver)
        upload.buttonCaption = null
        upload.setWidth("300px")

        // for mobile devices
        // upload.setImmediate(true);

        // progressbar update
        upload.addProgressListener { readBytes, contentLength -> bar.value = readBytes.toFloat() / contentLength.toFloat() }
        upload.addStartedListener {
            UI.getCurrent().pollInterval = 500
            progressLayout.isVisible = true
            upload.isVisible = false
            bar.value = 0f
            commentField.isVisible = false
            commentLabel.isVisible = false
            filenameLabel.isVisible = false
            filenameField.isVisible = false
        }
        upload.addChangeListener { event ->
            val filename = event?.filename ?: ""
            val p = Paths.get(filename)
            filenameField.value = p.fileName.toString()
        }

        commentLabel.isVisible = true
        commentField.isVisible = true
        commentField.setWidth("250px")

        filenameLabel.isVisible = true
        filenameField.isVisible = true
        filenameField.setWidth("250px")

        content.addComponent(upload)
        content.addComponent(filenameLabel)
        content.addComponent(filenameField)
        content.addComponent(commentLabel)
        content.addComponent(commentField)

        uploadButton.setWidth("100px")
        uploadButton.addClickListener { upload.submitUpload() }

        content.addComponent(uploadButton)

    }

    companion object {
        private val logger = Logger.getLogger(UploadWindow::class.java.name)
    }

}
