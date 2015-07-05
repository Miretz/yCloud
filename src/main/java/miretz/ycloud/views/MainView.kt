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

public class MainView
@Inject
constructor(Named("adminUser") protected var adminUser: String, Named("uploadDir") protected var uploadDir: String, protected var documentService: DocumentService, protected var databaseService: DatabaseService) : CustomComponent(), View {

    private var username = ""
    private var sizeStats: Label? = null
    private var header: HeaderPanel? = null
    private var uploadButton: Button? = null
    private var createFolderButton: Button? = null
    private var goToParentButton: Button? = null
    private var downloadAllButton: Button? = null
    private var deleteAllButton: Button? = null
    private var reloadButton: Button? = null
    private var filesTable: FilesTable? = null

    public var currentFolder: Document

    init {
        this.currentFolder = databaseService.findDocument("root")
    }

    public fun changeCurrentFolder(document: Document) {
        this.currentFolder = document
        toggleGoToParentButton()
    }

    private fun toggleGoToParentButton() {
        goToParentButton!!.setVisible(currentFolder.contentId != "root")
    }

    public fun goToParentFolder() {
        if (currentFolder.contentId != "root") {
            this.currentFolder = databaseService.findDocument(currentFolder.parentId)
        }
        toggleGoToParentButton()
    }

    public fun initialize() {

        sizeStats = Label()
        header = HeaderPanel()
        createFolderButton = Button("Create Folder")
        uploadButton = Button("Upload File")
        goToParentButton = Button("Go to parent folder")
        goToParentButton!!.setVisible(false)
        downloadAllButton = Button("Download .zip")
        deleteAllButton = Button("Delete All")
        reloadButton = Button("Refresh")
        filesTable = FilesTable(this, documentService, databaseService)

        val vl = VerticalLayout()
        vl.setSpacing(true)
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
        files.setImmediate(true)
        vl.addComponent(files)

        setCompositionRoot(vl)
    }

    override fun enter(event: ViewChangeEvent) {

        initialize()

        header!!.enableLogout()
        username = (getSession().getAttribute("user")) as String
        if (username == adminUser) {
            header!!.enableUsers()
        }

        uploadButton!!.setStyleName(ValoTheme.BUTTON_FRIENDLY)
        uploadButton!!.setIcon(ThemeResource("img/upload.png"))
        uploadButton!!.setDescription("Upload File")
        uploadButton!!.addClickListener(object : Button.ClickListener {

            override fun buttonClick(event: ClickEvent) {
                val uv = UploadWindow(documentService, databaseService, uploadDir, currentFolder)
                UI.getCurrent().addWindow(uv)
                uv.addCloseListener(object : Window.CloseListener {

                    override fun windowClose(e: CloseEvent) {
                        filesTable!!.loadFiles()
                        generateStats()

                    }
                })

            }
        })

        createFolderButton!!.setDescription("Create Folder")
        createFolderButton!!.addClickListener(object : Button.ClickListener {

            override fun buttonClick(event: ClickEvent) {
                val uv = CreateFolderWindow(databaseService, uploadDir, currentFolder)
                UI.getCurrent().addWindow(uv)
                uv.addCloseListener(object : Window.CloseListener {

                    override fun windowClose(e: CloseEvent) {
                        filesTable!!.loadFiles()
                        generateStats()

                    }
                })

            }
        })

        goToParentButton!!.setDescription("Go to Parent folder")
        goToParentButton!!.addClickListener(object : Button.ClickListener {

            override fun buttonClick(event: ClickEvent) {
                goToParentFolder()
                filesTable!!.loadFiles()
            }
        })

        reloadButton!!.setIcon(ThemeResource("img/reload.png"))
        reloadButton!!.setDescription("Reload Files")
        reloadButton!!.addClickListener(object : Button.ClickListener {

            override fun buttonClick(event: ClickEvent) {
                filesTable!!.loadFiles()
                generateStats()
                Notification.show("Files reloaded", "", Notification.Type.HUMANIZED_MESSAGE)
            }
        })

        val source = object : StreamResource.StreamSource {

            override fun getStream(): InputStream {
                return documentService.getAllFilesZip(databaseService.getDescendants(currentFolder.contentId))
            }
        }
        val sr = StreamResource(source, currentFolder.fileName + "_all_files.zip")
        val fileDownloader = FileDownloader(sr)
        downloadAllButton!!.setIcon(ThemeResource("img/zip.png"))
        downloadAllButton!!.setDescription("Download all as zip")
        fileDownloader.extend(downloadAllButton)

        deleteAllButton!!.setVisible(true)
        deleteAllButton!!.setIcon(ThemeResource("img/delete.png"))
        deleteAllButton!!.setDescription("Delete all files")
        deleteAllButton!!.addClickListener(object : Button.ClickListener {

            override fun buttonClick(event: ClickEvent) {

                val confirmation = ConfirmationWindow(databaseService.getDescendants(currentFolder.contentId), documentService, databaseService)

                UI.getCurrent().addWindow(confirmation)

                confirmation.addCloseListener(object : Window.CloseListener {

                    override fun windowClose(e: CloseEvent) {
                        filesTable!!.loadFiles()
                        generateStats()
                    }
                })

            }
        })
        generateStats()
    }

    public fun generateStats() {
        sizeStats!!.setValue("CURRENT FOLDER: " + currentFolder.fileName + " " + username + " in " + uploadDir + " (" + documentService.getSizeOfFiles() + " / " + documentService.getFreeSpace() + " MB)")
    }

    companion object {
        public val NAME: String = ""
    }
}