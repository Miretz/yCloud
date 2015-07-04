package miretz.ycloud.views.windows;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import miretz.ycloud.models.Document;
import miretz.ycloud.services.DatabaseService;
import miretz.ycloud.services.DocumentService;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class CreateFolderWindow extends Window {

	private static final long serialVersionUID = 1L;

	private final Label fileNameLabel = new Label("Folder Name:");
	private final TextField fileNameField = new TextField();

	private final Label commentLabel = new Label("Folder Comment:");
	private final TextField commentField = new TextField();

	public CreateFolderWindow(final DocumentService documentService, final DatabaseService databaseService, final String uploadDir, final Document currentFolder) {

		super("Create Folder");
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

		fileNameLabel.setVisible(true);
		fileNameField.setVisible(true);
		fileNameField.setWidth("250px");

		content.addComponent(fileNameLabel);
		content.addComponent(fileNameField);

		commentLabel.setVisible(true);
		commentField.setVisible(true);
		commentField.setWidth("250px");

		content.addComponent(commentLabel);
		content.addComponent(commentField);

		Button btnCreateUser = new Button("Create Folder");
		btnCreateUser.addClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {

				String creator = String.valueOf(getSession().getAttribute("user"));

				Map<String, String> metadata = new HashMap<>();
				metadata.put("creator", creator);
				metadata.put("comment", commentField.getValue());

				Document document = new Document(UUID.randomUUID().toString(), fileNameField.getValue(), currentFolder.getContentId(), metadata, Document.TYPE_FOLDER);

				databaseService.addDocument(document);

				close();

			}
		});

		content.addComponent(btnCreateUser);

	}

}
