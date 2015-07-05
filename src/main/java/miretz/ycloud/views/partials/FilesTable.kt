package miretz.ycloud.views.partials

import java.io.File
import java.util.Arrays

import miretz.ycloud.models.Document
import miretz.ycloud.services.DatabaseService
import miretz.ycloud.services.DocumentService
import miretz.ycloud.services.utils.FileIconUtil
import miretz.ycloud.services.utils.Icons
import miretz.ycloud.views.MainView
import miretz.ycloud.views.windows.ConfirmationWindow
import miretz.ycloud.views.windows.LightboxWindow

import com.google.gwt.safehtml.shared.SafeHtmlUtils
import com.vaadin.event.MouseEvents
import com.vaadin.server.FileDownloader
import com.vaadin.server.FileResource
import com.vaadin.server.ThemeResource
import com.vaadin.shared.ui.label.ContentMode
import com.vaadin.ui.*
import com.vaadin.ui.Button.ClickEvent
import com.vaadin.ui.Window.CloseEvent
import com.vaadin.ui.themes.ValoTheme
import org.atmosphere.interceptor.AtmosphereResourceStateRecovery
import java.lang
import kotlin.platform.platformName

public class FilesTable(private val mainView: MainView, protected var documentService: DocumentService, protected var databaseService: DatabaseService) : Panel() {

	private val lw: LightboxWindow
    private var lightboxWindowClosed = true

    private val table: Table

    init {

        table = Table()

        table.addContainerProperty("Icon", javaClass<Image>(), null)
        table.addContainerProperty("FileName / Comment", javaClass<VerticalLayout>(), null)
        table.addContainerProperty("Creator", javaClass<String>(), null)
        table.addContainerProperty("Modified", javaClass<String>(), null)
        table.addContainerProperty("Size (MB)",  javaClass<String>(), null)
        table.addContainerProperty("File Type", javaClass<String>(), null)
        table.addContainerProperty("Delete", javaClass<Button>(), null)

        loadFiles()

        table.setSelectable(false)
        table.setImmediate(true)
        table.addStyleName(ValoTheme.TABLE_NO_VERTICAL_LINES)
        table.addStyleName(ValoTheme.TABLE_NO_STRIPES)
        table.addStyleName(ValoTheme.TABLE_SMALL)
        table.setColumnCollapsingAllowed(true)
        table.setColumnCollapsed("File Type", true)
        // setColumnWidth("", 50);
        table.setSizeFull()

        setContent(table)
        setSizeFull()

        lw = LightboxWindow(documentService)
        lw.addCloseListener(object : Window.CloseListener {

            override fun windowClose(e: CloseEvent) {
                lightboxWindowClosed = true
            }
        })
    }

    public fun loadFiles() {
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
        table.setPageLength(table.size())
        setImmediate(true)
    }

    private fun addFileToTable(counter: Int, document: Document) {
        val file = documentService.getFile(document)

        val size = documentService.getSizeInMbDouble(file.length())

        val mimeType = documentService.getFileMimeType(document)

        var thumbnail: Image? = null
        val thumbnailResource = documentService.getThumbnailFileResource(document)
        if (thumbnailResource != null) {
            thumbnail = Image(null, thumbnailResource)
            thumbnail.addStyleName("cursor-pointer")
            thumbnail.addClickListener(object : MouseEvents.ClickListener {

                override fun click(event: com.vaadin.event.MouseEvents.ClickEvent) {
                    if (lightboxWindowClosed) {
                        lightboxWindowClosed = false
                        lw.setImage(document)
                        UI.getCurrent().addWindow(lw)
                    } else {
                        lw.setImage(document)
                        lw.setImmediate(true)
                    }
                }
            })
        } else {
            val icon = FileIconUtil.detectIcon(mimeType)
            val resource = ThemeResource(icon)
            thumbnail = Image(null, resource)
        }

        val modified = documentService.getModifiedDate(document)

        val download = Button(document.fileName)
        val downloadResource = documentService.getFileResource(document)
        val fileDownloader = FileDownloader(downloadResource)
        fileDownloader.extend(download)

        download.addStyleName(ValoTheme.BUTTON_LINK)
        download.addStyleName("fileLink")
        download.setComponentError(null)

        setErrorHandler(null)

        val delete = Button("Delete")
        delete.setIcon(ThemeResource("img/delete.png"))
        delete.addStyleName(ValoTheme.BUTTON_LINK)
        delete.addClickListener(object : Button.ClickListener {

            override fun buttonClick(event: ClickEvent) {

                val confirmation = ConfirmationWindow(Arrays.asList(document), documentService, databaseService)

                UI.getCurrent().addWindow(confirmation)

                confirmation.addCloseListener(object : Window.CloseListener {

                    override fun windowClose(e: CloseEvent) {
                        loadFiles()
                    }
                })

            }
        })

        val comment = SafeHtmlUtils.htmlEscape(document.metadata.get("comment"))
        val creator = document.metadata.get("creator") as String

        val commentLabel = Label("<span class=\"comment\">" + comment + "</span>", ContentMode.HTML)

        val vl = VerticalLayout(download, commentLabel)
        vl.setComponentAlignment(commentLabel, Alignment.TOP_LEFT)

        table.addItem(arrayOf<Any>(thumbnail, vl, creator, modified, size.toString(), mimeType, delete), counter)
    }

    private fun addFolderToTable(counter: Int, document: Document) {

        val fileName = document.fileName

        val icon = Icons.FOLDER.toString()
        val resource = ThemeResource(icon)
        val thumbnail = Image(null, resource)

        val download = Button(fileName)
        download.addStyleName(ValoTheme.BUTTON_LINK)
        download.addStyleName("fileLink")
        download.addClickListener(object : Button.ClickListener {

            override fun buttonClick(event: ClickEvent) {
                mainView.changeCurrentFolder(document)
                loadFiles()
            }
        })

        val delete = getDeleteButton(document)

        val comment = SafeHtmlUtils.htmlEscape(document.metadata.get("comment"))
        val creator = document.metadata.get("creator") as String

        val commentLabel = Label("<span class=\"comment\">" + comment + "</span>", ContentMode.HTML)

        val vl = VerticalLayout(download, commentLabel)
        vl.setComponentAlignment(commentLabel, Alignment.TOP_LEFT)

        table.addItem(arrayOf<Any>(thumbnail, vl, creator, "", "0.0", "folder", delete), counter)
    }

    private fun getDeleteButton(document: Document): Button {
        val delete = Button("Delete")
        delete.setIcon(ThemeResource("img/delete.png"))
        delete.addStyleName(ValoTheme.BUTTON_LINK)
        delete.addClickListener(object : Button.ClickListener {

            override fun buttonClick(event: ClickEvent) {

                val confirmation = ConfirmationWindow(Arrays.asList(document), documentService, databaseService)

                UI.getCurrent().addWindow(confirmation)

                confirmation.addCloseListener(object : Window.CloseListener {

                    override fun windowClose(e: CloseEvent) {
                        loadFiles()
                    }
                })

            }
        })
        return delete
    }
}
