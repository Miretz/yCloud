package miretz.ycloud.views.partials;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import miretz.ycloud.models.Document;
import miretz.ycloud.services.DatabaseService;
import miretz.ycloud.services.DocumentService;
import miretz.ycloud.services.utils.FileIconUtil;
import miretz.ycloud.services.utils.Icons;
import miretz.ycloud.views.MainView;
import miretz.ycloud.views.windows.ConfirmationWindow;
import miretz.ycloud.views.windows.LightboxWindow;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.vaadin.event.MouseEvents;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FileResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.themes.ValoTheme;

public class FilesTable extends Table {

	protected DocumentService documentService;
	protected DatabaseService databaseService;

	private static final long serialVersionUID = 1L;
	private final MainView mainView;
	private LightboxWindow lw;
	private boolean lightboxWindowClosed = true;

	public FilesTable(MainView mainView, DocumentService documentService, DatabaseService databaseService) {

		super();

		this.documentService = documentService;
		this.databaseService = databaseService;
		this.mainView = mainView;

		addContainerProperty("Icon", Image.class, null);
		addContainerProperty("FileName / Comment", VerticalLayout.class, null);
		addContainerProperty("Creator", String.class, null);
		addContainerProperty("Modified", String.class, null);
		addContainerProperty("Size (MB)", Double.class, null);
		addContainerProperty("File Type", String.class, null);
		addContainerProperty("Delete", Button.class, null);

		loadFiles();

		setSelectable(false);
		setImmediate(true);
		addStyleName(ValoTheme.TABLE_NO_VERTICAL_LINES);
		addStyleName(ValoTheme.TABLE_NO_STRIPES);
		addStyleName(ValoTheme.TABLE_SMALL);
		setColumnCollapsingAllowed(true);
		setColumnCollapsed("File Type", true);
		// setColumnWidth("", 50);
		setSizeFull();
		lw = new LightboxWindow(documentService);
		lw.addCloseListener(new Window.CloseListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void windowClose(CloseEvent e) {
				lightboxWindowClosed = true;
			}
		});
	}

	public void loadFiles() {
		removeAllItems();
		List<Document> fileNames = databaseService.getDescendants(mainView.getCurrentFolder().getContentId());
		int counter = 1;
		for (final Document document : fileNames) {

			if (document.getType().equals(Document.TYPE_FOLDER)) {
				addFolderToTable(counter, document);
			} else if (document.getType().equals(Document.TYPE_FILE)) {
				addFileToTable(counter, document);
			}

			counter++;
		}

		Object[] properties = { "Modified" };
		boolean[] ordering = { true };
		sort(properties, ordering);

		mainView.generateStats();
		setPageLength(this.size());
		setImmediate(true);
	}

	private void addFileToTable(int counter, final Document document) {
		File file = documentService.getFile(document.getFileName());

		final String fileName = document.getFileName();
		final Double size = documentService.getSizeInMbDouble(file.length());

		String mimeType = documentService.getFileMimeType(document.getFileName());

		Image thumbnail = null;
		FileResource thumbnailResource = documentService.getThumbnailFileResource(fileName);
		if (thumbnailResource != null) {
			thumbnail = new Image(null, thumbnailResource);
			thumbnail.addStyleName("cursor-pointer");
			thumbnail.addClickListener(new MouseEvents.ClickListener() {

				private static final long serialVersionUID = 1L;

				@Override
				public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
					if (lightboxWindowClosed) {
						lightboxWindowClosed = false;
						lw.setImage(fileName);
						UI.getCurrent().addWindow(lw);
					} else {
						lw.setImage(fileName);
						lw.setImmediate(true);
					}
				}

			});
		} else {
			String icon = FileIconUtil.detectIcon(mimeType);
			ThemeResource resource = new ThemeResource(icon);
			thumbnail = new Image(null, resource);
		}

		String modified = documentService.getModifiedDate(fileName);

		Button download = new Button(fileName);
		FileResource downloadResource = documentService.getFileResource(fileName);
		FileDownloader fileDownloader = new FileDownloader(downloadResource);
		fileDownloader.extend(download);
		download.addStyleName(ValoTheme.BUTTON_LINK);
		download.addStyleName("fileLink");
		download.setComponentError(null);

		setErrorHandler(null);

		Button delete = new Button("Delete");
		delete.setIcon(new ThemeResource("img/delete.png"));
		delete.addStyleName(ValoTheme.BUTTON_LINK);
		delete.addClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {

				ConfirmationWindow confirmation = new ConfirmationWindow(Arrays.asList(document), documentService, databaseService);

				UI.getCurrent().addWindow(confirmation);

				confirmation.addCloseListener(new Window.CloseListener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void windowClose(CloseEvent e) {
						loadFiles();
					}
				});

			}
		});

		String comment = SafeHtmlUtils.htmlEscape(document.getMetadata().get("comment"));
		String creator = document.getMetadata().get("creator");

		Label commentLabel = new Label("<span class=\"comment\">" + comment + "</span>", ContentMode.HTML);

		VerticalLayout vl = new VerticalLayout(download, commentLabel);
		vl.setComponentAlignment(commentLabel, Alignment.TOP_LEFT);

		addItem(new Object[] { thumbnail, vl, creator, modified, size, mimeType, delete }, counter);
	}

	private void addFolderToTable(int counter, final Document document) {

		final String fileName = document.getFileName();

		String icon = Icons.FOLDER.toString();
		ThemeResource resource = new ThemeResource(icon);
		Image thumbnail = new Image(null, resource);

		Button download = new Button(fileName);
		download.addStyleName(ValoTheme.BUTTON_LINK);
		download.addStyleName("fileLink");
		download.addClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				mainView.setCurrentFolder(document);
				loadFiles();
			}
		});

		Button delete = getDeleteButton(document);

		String comment = SafeHtmlUtils.htmlEscape(document.getMetadata().get("comment"));
		String creator = document.getMetadata().get("creator");

		Label commentLabel = new Label("<span class=\"comment\">" + comment + "</span>", ContentMode.HTML);

		VerticalLayout vl = new VerticalLayout(download, commentLabel);
		vl.setComponentAlignment(commentLabel, Alignment.TOP_LEFT);

		addItem(new Object[] { thumbnail, vl, creator, "", 0.0, "folder", delete }, counter);
	}

	private Button getDeleteButton(final Document document) {
		Button delete = new Button("Delete");
		delete.setIcon(new ThemeResource("img/delete.png"));
		delete.addStyleName(ValoTheme.BUTTON_LINK);
		delete.addClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {

				ConfirmationWindow confirmation = new ConfirmationWindow(Arrays.asList(document), documentService, databaseService);

				UI.getCurrent().addWindow(confirmation);

				confirmation.addCloseListener(new Window.CloseListener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void windowClose(CloseEvent e) {
						loadFiles();
					}
				});

			}
		});
		return delete;
	}

}
