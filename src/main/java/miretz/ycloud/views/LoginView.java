package miretz.ycloud.views;

import miretz.ycloud.services.DatabaseService;
import miretz.ycloud.services.utils.PasswordValidator;
import miretz.ycloud.views.partials.HeaderPanel;

import com.google.inject.Inject;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class LoginView extends CustomComponent implements View, Button.ClickListener {

	private static final long serialVersionUID = 1L;
	
	protected DatabaseService databaseService;

	public static final String NAME = "login";
	private TextField user;
	private PasswordField password;
	private Button loginButton;
	
	@Inject
	public LoginView(DatabaseService databaseService){
		this.databaseService = databaseService;
	}
	
	public void initialize() {
		setSizeFull();

		// Create the user input field
		user = new TextField("User:");
		user.setWidth("300px");
		user.setRequired(true);
		user.setInputPrompt("Your username (eg. joe@email.com)");
		user.addValidator(new EmailValidator("Username must be an email address"));
		user.setInvalidAllowed(false);

		// Create the password input field
		password = new PasswordField("Password:");
		password.setWidth("300px");
		password.addValidator(new PasswordValidator());
		password.setRequired(true);
		password.setValue("");
		password.setNullRepresentation("");

		// Create login button
		loginButton = new Button("Login", this);

		// Add both to a panel
		VerticalLayout fields = new VerticalLayout(user, password, loginButton);
		fields.setSpacing(true);
		fields.setMargin(new MarginInfo(true, true, true, false));
		fields.setSizeUndefined();

		// The view root layout
		VerticalLayout viewLayout = new VerticalLayout(fields);
		viewLayout.setSizeFull();
		viewLayout.setComponentAlignment(fields, Alignment.MIDDLE_CENTER);

		VerticalLayout mainLayout = new VerticalLayout(new HeaderPanel(), viewLayout);
		setCompositionRoot(mainLayout);
	}

	@Override
	public void enter(ViewChangeEvent event) {
		initialize();
		user.focus();
	}

	@Override
	public void buttonClick(ClickEvent event) {

		if (!user.isValid() || !password.isValid()) {
			return;
		}

		String username = user.getValue();

		boolean isValid = databaseService.checkUserPassword(username, password.getValue());

		if (isValid) {
			getSession().setAttribute("user", username);
			getUI().getNavigator().navigateTo(MainView.NAME);
		} else {
			this.password.setValue(null);
			this.password.focus();
		}
	}
}