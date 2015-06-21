package miretz.ycloud.views.windows;

import java.util.List;

import miretz.ycloud.models.Document;
import miretz.ycloud.services.DocumentService;

import com.vaadin.shared.ui.label.ContentMode;
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

	public enum Action {
		DELETE, DELETE_ALL;
	}

	public ConfirmationWindow(String fileName, final Action action) {

		super("User confirmation required");
		center();

		String text = "";
		
		switch(action){
		case DELETE: 
		
			text = "Are you sure you want to delete " + fileName + " ?";
		
			break;
			
		case DELETE_ALL: 
						
			List<Document> allFiles = DocumentService.getAllFilesAsDocuments();
			
			StringBuilder sb = new StringBuilder();
			
			for(Document doc: allFiles){
				sb.append(doc.getFileName());
				sb.append("\n");
			}
			fileName = sb.toString();
						
			text = "Files to delete: \n" + fileName;
						
			break;
		
		default: break;
		}
				
		// Some basic content for the window
		VerticalLayout content = new VerticalLayout();
		content.setMargin(true);
		setContent(content);

		// label
		Label textLabel = new Label(text, ContentMode.PREFORMATTED);
		content.addComponent(textLabel);
		content.setComponentAlignment(textLabel, Alignment.MIDDLE_CENTER);

		// buttons
		HorizontalLayout buttons = new HorizontalLayout();

		final String fileNameFinal = fileName;
		
		Button yes = new Button("Yes", new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				executeAction(action, fileNameFinal);
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

	private void executeAction(Action action, String fileName) {
		switch (action) {
		case DELETE:

			DocumentService.deleteFile(fileName);
			Notification.show("File Deleted:", fileName, Notification.Type.HUMANIZED_MESSAGE);
			break;
		
		case DELETE_ALL:
		
			DocumentService.deleteAllFiles();
		    Notification.show("Files deleted: ", fileName, Notification.Type.HUMANIZED_MESSAGE);
			break;
		
		default:
			break;
		}

	}

}
