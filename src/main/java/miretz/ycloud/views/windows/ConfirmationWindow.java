package miretz.ycloud.views.windows;

import java.util.List;

import miretz.ycloud.models.Document;
import miretz.ycloud.services.DatabaseService;
import miretz.ycloud.services.DocumentService;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class ConfirmationWindow extends Window {

	public ConfirmationWindow(final List<Document> documents, final DocumentService documentService, final DatabaseService databaseService) {

		super("User confirmation required");

		center();

		final String filesToDelete = filenamesToString(documents);
		final String text = "Files to delete: \n" + filesToDelete;

		// Some basic content for the window
		VerticalLayout content = new VerticalLayout();
		content.setMargin(true);
		setContent(content);

		// label
		Label textLabel = new Label(text);
		content.addComponent(textLabel);
		content.setComponentAlignment(textLabel, Alignment.MIDDLE_CENTER);

		// buttons
		HorizontalLayout buttons = new HorizontalLayout();

		Button yes = new Button("Yes", new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				for (Document document : documents) {
					documentService.deleteFile(document.getFileName());
					databaseService.deleteDocument(document.getContentId());
				}
				Notification.show("Files Deleted:", filesToDelete, Notification.Type.HUMANIZED_MESSAGE);
				close();
			}
		});
		Button no = new Button("No", new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				close();
			}
		});

		buttons.addComponent(yes);
		buttons.addComponent(no);
		content.addComponent(buttons);
		content.setComponentAlignment(buttons, Alignment.MIDDLE_CENTER);

	}

	private String filenamesToString(final List<Document> documents) {
		StringBuilder sb = new StringBuilder();
		for (Document doc : documents) {
			sb.append(doc.getFileName());
			sb.append("\n");
		}
		return sb.toString();
	}

}
