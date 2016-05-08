package miretz.ycloud.views

import java.io.InputStream

import miretz.ycloud.models.Document
import miretz.ycloud.services.DatabaseService
import miretz.ycloud.services.DocumentService
import miretz.ycloud.views.partials.FilesTable
import miretz.ycloud.views.partials.HeaderPanel
import miretz.ycloud.views.windows.ConfirmationWindow
import miretz.ycloud.views.windows.CreateFolderWindow
import miretz.ycloud.views.windows.UploadWindow

import com.google.inject.Inject
import com.google.inject.name.Named
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.vaadin.server.FileDownloader
import com.vaadin.server.StreamResource
import com.vaadin.server.ThemeResource
import com.vaadin.ui.Alignment
import com.vaadin.ui.Button
import com.vaadin.ui.Button.ClickEvent
import com.vaadin.ui.CustomComponent
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.Label
import com.vaadin.ui.Notification
import com.vaadin.ui.UI
import com.vaadin.ui.VerticalLayout
import com.vaadin.ui.Window
import com.vaadin.ui.Window.CloseEvent
import com.vaadin.ui.themes.ValoTheme

class MainView
@Inject
constructor(@Named("adminUser") protected var adminUser: String, @Named("uploadDir") protected var uploadDir: String, protected val documentService: DocumentService, protected val databaseService: DatabaseService) : CustomComponent(), View {

    private var username = ""
    var currentFolder: Document

    private val sizeStats: Label
    private val header: HeaderPanel
    private val uploadButton: Button
    private val createFolderButton: Button
    private val goToParentButton: Button
    private val downloadAllButton: Button
    private val deleteAllButton: Button
    private val reloadButton: Button
    private val filesTable: FilesTable

    init {
        this.currentFolder = databaseService.findDocument("root")

        this.sizeStats = Label()
        this.header = HeaderPanel()
        this.uploadButton = Button("Upload File")
        this.createFolderButton = Button("Create Folder")
        this.goToParentButton = Button("Go to parent folder")
        this.downloadAllButton =  Button("Download .zip")
        this.deleteAllButton = Button("Delete All")
        this.reloadButton = Button("Reload")

        this.filesTable = FilesTable(this, documentService, databaseService)
    }

    fun changeCurrentFolder(document: Document) {
        this.currentFolder = document
        toggleGoToParentButton()
    }

    private fun toggleGoToParentButton() {
        goToParentButton.isVisible = currentFolder.contentId != "root"
    }

    fun goToParentFolder() {
        if (currentFolder.contentId != "root") {
            this.currentFolder = databaseService.findDocument(currentFolder.parentId)
        }
        toggleGoToParentButton()
    }

    fun initialize() {

        goToParentButton.isVisible = false

        val vl = VerticalLayout()
        vl.isSpacing = true
        vl.setSizeFull()
        vl.addComponent(header)

        val info = HorizontalLayout(sizeStats)
        vl.addComponent(info)
        vl.setComponentAlignment(info, Alignment.MIDDLE_CENTER)

        val buttons = HorizontalLayout(uploadButton, createFolderButton, goToParentButton, reloadButton, downloadAllButton, deleteAllButton)

        vl.addComponent(buttons)
        vl.setComponentAlignment(buttons, Alignment.MIDDLE_CENTER)

        val files = HorizontalLayout(filesTable)
        // files.setMargin(true);
        files.setSizeFull()
        files.isImmediate = true
        vl.addComponent(files)

        compositionRoot = vl
    }

    override fun enter(event: ViewChangeEvent) {

        initialize()

        header.enableLogout()
        username = (session.getAttribute("user")) as String
        if (username == adminUser) {
            header.enableUsers()
        }

        uploadButton.styleName = ValoTheme.BUTTON_FRIENDLY
        uploadButton.icon = ThemeResource("img/upload.png")
        uploadButton.description = "Upload File"
        uploadButton.addClickListener {
            val uv = UploadWindow(documentService, databaseService, uploadDir, currentFolder)
            UI.getCurrent().addWindow(uv)
            uv.addCloseListener {
                filesTable.loadFiles()
                generateStats()
            }
        }

        createFolderButton.description = "Create Folder"
        createFolderButton.addClickListener {
            val uv = CreateFolderWindow(databaseService, currentFolder)
            UI.getCurrent().addWindow(uv)
            uv.addCloseListener {
                filesTable.loadFiles()
                generateStats()
            }
        }

        goToParentButton.description = "Go to Parent folder"
        goToParentButton.addClickListener {
            goToParentFolder()
            filesTable.loadFiles()
        }

        reloadButton.icon = ThemeResource("img/reload.png")
        reloadButton.description = "Reload Files"
        reloadButton.addClickListener {
            filesTable.loadFiles()
            generateStats()
            Notification.show("Files reloaded", "", Notification.Type.HUMANIZED_MESSAGE)
        }

        val source = StreamResource.StreamSource { documentService.getAllFilesZip(databaseService.getDescendants(currentFolder.contentId)) }
        val sr = StreamResource(source, currentFolder.fileName + "_all_files.zip")
        val fileDownloader = FileDownloader(sr)
        downloadAllButton.icon = ThemeResource("img/zip.png")
        downloadAllButton.description = "Download all as zip"
        fileDownloader.extend(downloadAllButton)

        deleteAllButton.isVisible = true
        deleteAllButton.icon = ThemeResource("img/delete.png")
        deleteAllButton.description = "Delete all files"
        deleteAllButton.addClickListener {
            val confirmation = ConfirmationWindow(databaseService.getDescendants(currentFolder.contentId), documentService, databaseService)

            UI.getCurrent().addWindow(confirmation)

            confirmation.addCloseListener(object : Window.CloseListener {

                override fun windowClose(e: CloseEvent) {
                    filesTable.loadFiles()
                    generateStats()
                }
            })
        }
        generateStats()
    }

    fun generateStats() {
        sizeStats.value = "CURRENT FOLDER: " + currentFolder.fileName + " " + username + " in " + uploadDir + " (" + documentService.getSizeOfFiles() + " / " + documentService.getFreeSpace() + " )"
    }

    companion object {
        val NAME: String = ""
    }
}