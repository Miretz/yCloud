package miretz.ycloud.views;

import java.io.InputStream;

import miretz.ycloud.services.ConfigurationService;
import miretz.ycloud.services.DocumentService;
import miretz.ycloud.views.partials.FilesTable;
import miretz.ycloud.views.partials.HeaderPanel;
import miretz.ycloud.views.windows.ConfirmationWindow;
import miretz.ycloud.views.windows.UploadWindow;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.themes.ValoTheme;

public class MainView extends CustomComponent implements View {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "";

	private String username = "";
	private Label sizeStats = new Label();
	private HeaderPanel header = new HeaderPanel();
	private Button uploadButton = new Button("Upload File");
	private Button downloadAllButton = new Button("Download .zip");
	private Button deleteAllButton = new Button("Delete All");
	private Button reloadButton = new Button("Refresh");
	private FilesTable filesView = new FilesTable(this);

	public MainView() {
		VerticalLayout vl = new VerticalLayout();
		vl.setSpacing(true);
		vl.setSizeFull();
		
		vl.addComponent(header);

		HorizontalLayout info = new HorizontalLayout(sizeStats);
		vl.addComponent(info);
		vl.setComponentAlignment(info, Alignment.MIDDLE_CENTER);
		
		HorizontalLayout buttons = new HorizontalLayout(uploadButton, reloadButton, downloadAllButton, deleteAllButton);
		
		vl.addComponent(buttons);
		vl.setComponentAlignment(buttons, Alignment.MIDDLE_CENTER);
				
		HorizontalLayout files = new HorizontalLayout(filesView);
		//files.setMargin(true);
		files.setSizeFull();
		files.setImmediate(true);
		vl.addComponent(files);
			
		setCompositionRoot(vl);
	}

	@Override
	public void enter(ViewChangeEvent event) {
		header.enableLogout();
		username = String.valueOf(getSession().getAttribute("user"));
		if (username.equals(ConfigurationService.getProperty("adminUser"))) {
			header.enableUsers();
		}

		uploadButton.setStyleName(ValoTheme.BUTTON_FRIENDLY);
		uploadButton.setIcon(new ThemeResource("img/upload.png"));
		uploadButton.setDescription("Upload File");
		uploadButton.addClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				UploadWindow uv = new UploadWindow();
				UI.getCurrent().addWindow(uv);
				uv.addCloseListener(new Window.CloseListener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void windowClose(CloseEvent e) {
						filesView.loadFiles();
						generateStats();

					}
				});

			}
		});
		reloadButton.setIcon(new ThemeResource("img/reload.png"));
		reloadButton.setDescription("Reload Files");
		reloadButton.addClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				filesView.loadFiles();
				generateStats();
				Notification.show("Files reloaded", "", Notification.Type.HUMANIZED_MESSAGE);
			}
		});

		StreamResource.StreamSource source = new StreamResource.StreamSource() {
			private static final long serialVersionUID = 1L;

			public InputStream getStream() {
				return DocumentService.getAllFilesZip();
			}
		};
		StreamResource sr = new StreamResource(source, "all_files.zip");
		FileDownloader fileDownloader = new FileDownloader(sr);
		downloadAllButton.setIcon(new ThemeResource("img/zip.png"));
		downloadAllButton.setDescription("Download all as zip");
		fileDownloader.extend(downloadAllButton);

		deleteAllButton.setVisible(true);
		deleteAllButton.setIcon(new ThemeResource("img/delete.png"));
		deleteAllButton.setDescription("Delete all files");
		deleteAllButton.addClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {

				ConfirmationWindow confirmation = new ConfirmationWindow(null, ConfirmationWindow.Action.DELETE_ALL);

				UI.getCurrent().addWindow(confirmation);

				confirmation.addCloseListener(new Window.CloseListener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void windowClose(CloseEvent e) {
						filesView.loadFiles();
						generateStats();
					}
				});

			}
		});
		generateStats();
	}

	public void generateStats() {
		sizeStats.setValue(username + " in " + DocumentService.UPLOAD_DIR + " (" + DocumentService.getSizeOfFiles() + " / " + DocumentService.getFreeSpace() + " MB)");
	}
}