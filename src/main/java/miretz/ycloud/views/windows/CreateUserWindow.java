package miretz.ycloud.views.windows;

import miretz.ycloud.services.DatabaseService;
import miretz.ycloud.services.PasswordValidatorService;

import com.vaadin.data.validator.EmailValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class CreateUserWindow extends Window {

	private static final long serialVersionUID = 1L;

	private final TextField fldUsername;
	private final PasswordField fldPassword;
	private final Button btnCreateUser;

	public CreateUserWindow() {

		super("Create User");
		center();

		// Some basic content for the window
		VerticalLayout content = new VerticalLayout();
		content.setMargin(true);
		content.setSpacing(true);
		setContent(content);

		// Disable the close button
		setClosable(true);
		setResizable(false);
		setModal(true);

		// Create the user input field
		fldUsername = new TextField("User:");
		fldUsername.setWidth("300px");
		fldUsername.setRequired(true);
		fldUsername.setInputPrompt("Your username (eg. joe@email.com)");
		fldUsername.addValidator(new EmailValidator(
				"Username must be an email address"));
		fldUsername.setInvalidAllowed(false);
		content.addComponent(fldUsername);

		// Create the password input field
		fldPassword = new PasswordField("Password:");
		fldPassword.setWidth("300px");
		fldPassword.addValidator(new PasswordValidatorService());
		fldPassword.setRequired(true);
		fldPassword.setValue("");
		fldPassword.setNullRepresentation("");
		content.addComponent(fldPassword);

		btnCreateUser = new Button("Create User");
		btnCreateUser.addClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				if (!fldUsername.isValid() || !fldPassword.isValid()) {
					fldUsername.setValue("Username not valid!");
					fldPassword.setValue(null);
					fldUsername.focus();
				} else {
					String username = fldUsername.getValue();
					String password = fldPassword.getValue();
					DatabaseService.addUser(username, password);
					close();
				}
			}
		});
		content.addComponent(btnCreateUser);

		fldUsername.focus();

	}

}
