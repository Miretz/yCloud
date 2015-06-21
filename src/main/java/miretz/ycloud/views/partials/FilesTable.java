package miretz.ycloud.views.partials;

import java.util.List;

import miretz.ycloud.models.Document;
import miretz.ycloud.services.DocumentService;
import miretz.ycloud.services.FileIconUtil;
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

	private static final long serialVersionUID = 1L;
	private final MainView mainView;
	private LightboxWindow lw;
	private boolean lightboxWindowClosed = true;

	public FilesTable(MainView mainView) {
		super();
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
		//setColumnWidth("", 50);
		setSizeFull();
		lw = new LightboxWindow();
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
		List<Document> fileNames = DocumentService.getAllFilesAsDocuments();
		int counter = 1;
		for (final Document document : fileNames) {

			final String fileName = document.getFileName();
			final Double size = DocumentService.getSizeInMbDouble(document.getSize());

			String mimeType = document.getMimeType();

			Image thumbnail = null;
			FileResource thumbnailResource = DocumentService.getThumbnailFileResource(fileName);
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

			String modified = DocumentService.getModifiedDate(fileName);

			Button download = new Button(fileName);
			FileResource downloadResource = DocumentService.getFileResource(fileName);
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

					ConfirmationWindow confirmation = new ConfirmationWindow(fileName, ConfirmationWindow.Action.DELETE);

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

			String comment = SafeHtmlUtils.htmlEscape(document.getComment());
			String creator = document.getCreator();

			Label commentLabel = new Label("<span class=\"comment\">" + comment + "</span>", ContentMode.HTML);

			VerticalLayout vl = new VerticalLayout(download, commentLabel);
			vl.setComponentAlignment(commentLabel, Alignment.TOP_LEFT);

			addItem(new Object[] { thumbnail, vl, creator, modified, size, mimeType, delete }, counter);
			counter++;
		}

		Object[] properties = { "Modified" };
		boolean[] ordering = { true };
		sort(properties, ordering);

		mainView.generateStats();
		setPageLength(this.size());
		setImmediate(true);
	}

}
