package miretz.ycloud.views.partials

import com.google.gwt.safehtml.shared.SafeHtmlUtils
import com.vaadin.server.FileDownloader
import com.vaadin.server.ThemeResource
import com.vaadin.shared.ui.label.ContentMode
import com.vaadin.ui.*
import com.vaadin.ui.Window.CloseEvent
import com.vaadin.ui.themes.ValoTheme
import miretz.ycloud.models.Document
import miretz.ycloud.services.DatabaseService
import miretz.ycloud.services.DocumentService
import miretz.ycloud.services.utils.FileIconUtil
import miretz.ycloud.services.utils.Icons
import miretz.ycloud.views.MainView
import miretz.ycloud.views.windows.ConfirmationWindow
import miretz.ycloud.views.windows.LightboxWindow
import java.util.*

class FilesTable(private val mainView: MainView, protected var documentService: DocumentService, protected var databaseService: DatabaseService) : Panel() {

	private val lw: LightboxWindow
    private val table: Table

    private var lightboxWindowClosed = true

    init {

        table = Table()

        table.addContainerProperty("Icon", Image::class.java, null)
        table.addContainerProperty("FileName / Comment", VerticalLayout::class.java, null)
        table.addContainerProperty("Creator", String::class.java, null)
        table.addContainerProperty("Modified", String::class.java, null)
        table.addContainerProperty("Size (MB)",  String::class.java, null)
        table.addContainerProperty("File Type", String::class.java, null)
        table.addContainerProperty("Delete", Button::class.java, null)

        loadFiles()

        table.isSelectable = false
        table.isImmediate = true
        table.addStyleName(ValoTheme.TABLE_NO_VERTICAL_LINES)
        table.addStyleName(ValoTheme.TABLE_NO_STRIPES)
        table.addStyleName(ValoTheme.TABLE_SMALL)
        table.isColumnCollapsingAllowed = true
        table.setColumnCollapsed("File Type", true)
        // setColumnWidth("", 50);
        table.setSizeFull()

        content = table
        setSizeFull()

        lw = LightboxWindow(documentService)
        lw.addCloseListener(object : Window.CloseListener {

            override fun windowClose(e: CloseEvent) {
                lightboxWindowClosed = true
            }
        })
    }

    fun loadFiles() {
        table.removeAllItems()
        val fileNames = databaseService.getDescendants(mainView.currentFolder.contentId)
        var counter = 1
        for (document in fileNames) {

            if (document.type == Document.TYPE_FOLDER) {
                addFolderToTable(counter, document)
            } else if (document.type == Document.TYPE_FILE) {
                addFileToTable(counter, document)
            }

            counter++
        }

        val properties = arrayOf<Any>("Modified")
        val ordering = booleanArrayOf(true)
        table.sort(properties, ordering)

        mainView.generateStats()
        table.pageLength = table.size()
        isImmediate = true
    }

    private fun addFileToTable(counter: Int, document: Document) {
        val file = documentService.getFile(document)

        val size = documentService.getSizeInMbDouble(file.length())

        val mimeType = documentService.getFileMimeType(document)

        var thumbnail: Image = getThumbnail(document, mimeType)

        val modified = documentService.getModifiedDate(document)

        val download = Button(document.fileName)
        val downloadResource = documentService.getFileResource(document)
        val fileDownloader = FileDownloader(downloadResource)
        fileDownloader.extend(download)

        download.addStyleName(ValoTheme.BUTTON_LINK)
        download.addStyleName("fileLink")
        download.componentError = null

        errorHandler = null

        val delete = Button("Delete")
        delete.icon = ThemeResource("img/delete.png")
        delete.addStyleName(ValoTheme.BUTTON_LINK)
        delete.addClickListener {
            val confirmation = ConfirmationWindow(Arrays.asList(document), documentService, databaseService)

            UI.getCurrent().addWindow(confirmation)

            confirmation.addCloseListener { loadFiles() }
        }

        val comment = SafeHtmlUtils.htmlEscape(document.metadata["comment"])
        val creator = document.metadata.get("creator") as String

        val commentLabel = Label("<span class=\"comment\">$comment</span>", ContentMode.HTML)

        val vl = VerticalLayout(download, commentLabel)
        vl.setComponentAlignment(commentLabel, Alignment.TOP_LEFT)

        table.addItem(arrayOf(thumbnail, vl, creator, modified, size.toString(), mimeType, delete), counter)
    }

    private fun getThumbnail(document: Document, mimeType: String): Image {

        val thumbnailResource = documentService.getThumbnailFileResource(document)
        if (thumbnailResource != null) {
            val thumbnail = Image(null, thumbnailResource)
            thumbnail.addStyleName("cursor-pointer")
            thumbnail.addClickListener {
                if (lightboxWindowClosed) {
                    lightboxWindowClosed = false
                    lw.setImage(document)
                    UI.getCurrent().addWindow(lw)
                } else {
                    lw.setImage(document)
                    lw.isImmediate = true
                }
            }
            return thumbnail
        } else {
            val icon = FileIconUtil.detectIcon(mimeType)
            val resource = ThemeResource(icon)
            return Image(null, resource)
        }
    }

    private fun addFolderToTable(counter: Int, document: Document) {

        val fileName = document.fileName

        val icon = Icons.FOLDER.toString()
        val resource = ThemeResource(icon)
        val thumbnail = Image(null, resource)

        val download = Button(fileName)
        download.addStyleName(ValoTheme.BUTTON_LINK)
        download.addStyleName("fileLink")
        download.addClickListener {
            mainView.changeCurrentFolder(document)
            loadFiles()
        }

        val delete = getDeleteButton(document)

        val comment = SafeHtmlUtils.htmlEscape(document.metadata.get("comment"))
        val creator = document.metadata.get("creator") as String

        val commentLabel = Label("<span class=\"comment\">$comment</span>", ContentMode.HTML)

        val vl = VerticalLayout(download, commentLabel)
        vl.setComponentAlignment(commentLabel, Alignment.TOP_LEFT)

        table.addItem(arrayOf(thumbnail, vl, creator, "", "0.0", "folder", delete), counter)
    }

    private fun getDeleteButton(document: Document): Button {
        val delete = Button("Delete")
        delete.icon = ThemeResource("img/delete.png")
        delete.addStyleName(ValoTheme.BUTTON_LINK)
        delete.addClickListener {
            val confirmation = ConfirmationWindow(Arrays.asList(document), documentService, databaseService)

            UI.getCurrent().addWindow(confirmation)

            confirmation.addCloseListener { loadFiles() }
        }
        return delete
    }
}
