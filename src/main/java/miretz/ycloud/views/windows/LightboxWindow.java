package miretz.ycloud.views.windows;

import miretz.ycloud.services.DocumentService;

import com.vaadin.event.MouseEvents;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Window;

public class LightboxWindow extends Window {

	protected DocumentService documentService;
	
	private static final long serialVersionUID = 1L;

	public LightboxWindow(DocumentService documentService) {
		super("Image Preview");
		center();
		this.documentService = documentService;
	}

	public void setImage(final String fileName) {

		setCaption("Image Preview: " + fileName);
		
		HorizontalLayout layout = new HorizontalLayout();
		setContent(layout);

		layout.setSizeFull();
		layout.setImmediate(true);

		Image image = new Image(null, documentService.getFileResource(fileName));
		layout.addComponent(image);
		layout.setComponentAlignment(image, Alignment.MIDDLE_CENTER);

		image.setHeight("75%");
		image.addStyleName("cursor-pointer");
		image.addClickListener(new MouseEvents.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void click(ClickEvent event) {
				close();
			}

		});

		setSizeFull();		
		setImmediate(true);

	}
}
