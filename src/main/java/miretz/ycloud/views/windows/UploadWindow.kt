package miretz.ycloud.views.windows

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.HashMap
import java.util.UUID

import miretz.ycloud.models.Document
import miretz.ycloud.services.DatabaseService
import miretz.ycloud.services.DocumentService

import org.apache.log4j.Logger

import com.vaadin.server.Page
import com.vaadin.ui.Alignment
import com.vaadin.ui.Label
import com.vaadin.ui.Notification
import com.vaadin.ui.ProgressBar
import com.vaadin.ui.TextField
import com.vaadin.ui.UI
import com.vaadin.ui.Upload
import com.vaadin.ui.Upload.Receiver
import com.vaadin.ui.Upload.StartedEvent
import com.vaadin.ui.Upload.SucceededEvent
import com.vaadin.ui.Upload.SucceededListener
import com.vaadin.ui.VerticalLayout
import com.vaadin.ui.Window

public class UploadWindow(documentService: DocumentService, databaseService: DatabaseService, uploadDir: String, currentFolder: Document) : Window("Upload File") {

    private val upload: Upload
    private val bar = ProgressBar()
    private val uploadCaption = Label("Uploading in progress. Please wait...")
    private val commentLabel = Label("File comment:")
    private val commentField = TextField()

    init {
        center()

        // Some basic content for the window
        val content = VerticalLayout()
        content.setMargin(true)
        setContent(content)
        content.setWidth("310px")

        // Disable the close button
        setClosable(true)
        setResizable(false)
        setModal(true)

        bar.setWidth("250px")
        val progressLayout = VerticalLayout()
        progressLayout.addComponent(bar)
        progressLayout.setComponentAlignment(bar, Alignment.MIDDLE_CENTER)
        progressLayout.addComponent(uploadCaption)
        progressLayout.setVisible(false)
        progressLayout.setSpacing(true)
        progressLayout.setWidth("100%")
        content.addComponent(progressLayout)
        content.setComponentAlignment(progressLayout, Alignment.MIDDLE_CENTER)

        class FileUploader : Receiver, SucceededListener {
            public var file: File? = null
            public var filename: String = ""
            public var uid: String = ""

            override fun receiveUpload(filenameV: String?, mimeType: String): OutputStream {
                try {
                    filename = filenameV ?: throw FileNotFoundException("No File Selected!")
                    uid = UUID.randomUUID().toString()
                    file = File(uploadDir + uid)
                } catch (e: java.io.FileNotFoundException) {
                    Notification("Could not open file", e.getMessage(), Notification.Type.ERROR_MESSAGE).show(Page.getCurrent())
                    logger.error("Could not open file!", e)
                    close()
                }

                return FileOutputStream(file)
            }

            override fun uploadSucceeded(event: SucceededEvent) {
                UI.getCurrent().setPollInterval(-1)

                val creator = (getSession().getAttribute("user")) as String

                val metadata = HashMap<String, String>()
                metadata.put("creator", creator)
                metadata.put("comment", commentField.getValue())

                val document = Document(uid, filename, currentFolder.contentId, metadata, Document.TYPE_FILE)

                documentService.saveThumbnail(document)

                databaseService.addDocument(document)

                Notification.show("Upload completed:", event.getFilename(), Notification.Type.HUMANIZED_MESSAGE)
                close()
            }
        }

        val receiver = FileUploader()
        upload = Upload("Please select your file.", receiver)
        upload.addSucceededListener(receiver)
        upload.setButtonCaption("Upload")
        upload.setWidth("300px")

        // for mobile devices
        // upload.setImmediate(true);

        // progressbar update
        upload.addProgressListener(object : Upload.ProgressListener {

            override fun updateProgress(readBytes: Long, contentLength: Long) {
                bar.setValue(readBytes.toFloat() / contentLength.toFloat())
            }
        })
        upload.addStartedListener(object : Upload.StartedListener {

            override fun uploadStarted(event: StartedEvent) {
                UI.getCurrent().setPollInterval(500)
                progressLayout.setVisible(true)
                upload.setVisible(false)
                bar.setValue(0f)
                commentField.setVisible(false)
                commentLabel.setVisible(false)
            }
        })

        commentLabel.setVisible(true)
        commentField.setVisible(true)
        commentField.setWidth("250px")

        content.addComponent(commentLabel)
        content.addComponent(commentField)
        content.addComponent(upload)

    }

    companion object {
        private val logger = Logger.getLogger(javaClass<UploadWindow>().getName())
    }

}
