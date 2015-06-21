package miretz.ycloud.views.windows;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import miretz.ycloud.services.ConfigurationService;
import miretz.ycloud.services.DatabaseService;
import miretz.ycloud.services.DocumentService;

import com.vaadin.server.Page;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class UploadWindow extends Window {

	private static final long serialVersionUID = 1L;

	private final static Logger logger = Logger.getLogger(UploadWindow.class.getName());

	private final Upload upload;
	private final ProgressBar bar = new ProgressBar();;
	private final Label uploadCaption = new Label("Uploading in progress. Please wait...");
	private final Label commentLabel = new Label("File comment:");
	private final TextField commentField = new TextField();

	public UploadWindow() {

		super("Upload File");
		center();

		// Some basic content for the window
		VerticalLayout content = new VerticalLayout();
		content.setMargin(true);
		setContent(content);
		content.setWidth("310px");

		// Disable the close button
		setClosable(true);
		setResizable(false);
		setModal(true);

		bar.setWidth("250px");
		final VerticalLayout progressLayout = new VerticalLayout();
		progressLayout.addComponent(bar);
		progressLayout.setComponentAlignment(bar, Alignment.MIDDLE_CENTER);
		progressLayout.addComponent(uploadCaption);
		progressLayout.setVisible(false);
		progressLayout.setSpacing(true);
		progressLayout.setWidth("100%");
		content.addComponent(progressLayout);
		content.setComponentAlignment(progressLayout, Alignment.MIDDLE_CENTER);

		class FileUploader implements Receiver, SucceededListener {
			private static final long serialVersionUID = 1L;
			public File file;

			public OutputStream receiveUpload(String filename, String mimeType) {
				FileOutputStream fos = null;
				try {
					if (filename == null || filename.isEmpty()) {
						throw new FileNotFoundException("No File Selected!");
					}
					file = new File(ConfigurationService.getProperty("uploadDir") + filename);
					fos = new FileOutputStream(file);
				} catch (final java.io.FileNotFoundException e) {
					new Notification("Could not open file", e.getMessage(), Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
					logger.log(Level.SEVERE, "Could not open file!", e);
					close();
				}
				return fos;
			}

			public void uploadSucceeded(SucceededEvent event) {
				UI.getCurrent().setPollInterval(-1);
				try {
					String creator = String.valueOf(getSession().getAttribute("user"));
					DocumentService.saveThumbnail(event.getFilename());
					DatabaseService.addFile(event.getFilename(), commentField.getValue(), creator);
				} catch (IOException e) {
					logger.log(Level.SEVERE, "Failed to create thumbnail!", e);
				} finally {
					Notification.show("Upload completed:", event.getFilename(), Notification.Type.HUMANIZED_MESSAGE);
					close();
				}
			}
		}
		;

		FileUploader receiver = new FileUploader();
		upload = new Upload("Please select your file.", receiver);
		upload.addSucceededListener(receiver);
		upload.setButtonCaption("Upload");
		upload.setWidth("300px");

		// for mobile devices
		// upload.setImmediate(true);

		// progressbar update
		upload.addProgressListener(new Upload.ProgressListener() {
			private static final long serialVersionUID = 1L;

			public void updateProgress(long readBytes, long contentLength) {
				bar.setValue(new Float(readBytes / (float) contentLength));
			}
		});
		upload.addStartedListener(new Upload.StartedListener() {
			private static final long serialVersionUID = 1L;

			public void uploadStarted(StartedEvent event) {
				UI.getCurrent().setPollInterval(500);
				progressLayout.setVisible(true);
				upload.setVisible(false);
				bar.setValue(0f);
				commentField.setVisible(false);
				commentLabel.setVisible(false);
			}
		});

		commentLabel.setVisible(true);
		commentField.setVisible(true);
		commentField.setWidth("250px");

		// upload.setVisible(false);
		// commentField.addTextChangeListener(new TextChangeListener(){
		//
		// private static final long serialVersionUID = 1L;
		//
		// @Override
		// public void textChange(TextChangeEvent event) {
		// upload.setVisible(true);
		// }
		//
		// });

		content.addComponent(commentLabel);
		content.addComponent(commentField);
		content.addComponent(upload);

	}

}
