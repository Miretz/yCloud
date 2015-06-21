package miretz.ycloud.views;

import miretz.ycloud.services.ConfigurationService;
import miretz.ycloud.services.DatabaseService;
import miretz.ycloud.views.partials.HeaderPanel;
import miretz.ycloud.views.windows.CreateUserWindow;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;

public class UsersView extends CustomComponent implements View {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "users";

	private Label text = new Label();
	private HeaderPanel header = new HeaderPanel();
	private Button btnCreateUser = new Button("Create User");
	private Button backButton = new Button("Return");
	private VerticalLayout users = new VerticalLayout();

	public UsersView() {
		VerticalLayout vl = new VerticalLayout();
		vl.setSpacing(true);
		vl.setSizeFull();
		vl.addComponent(header);

		HorizontalLayout statistics = new HorizontalLayout();
		statistics.addComponent(text);
		statistics.setSpacing(true);

		vl.addComponent(statistics);
		vl.setComponentAlignment(statistics, Alignment.MIDDLE_CENTER);

		HorizontalLayout buttons = new HorizontalLayout(btnCreateUser, backButton);
		vl.addComponent(buttons);
		vl.setComponentAlignment(buttons, Alignment.MIDDLE_RIGHT);
		users.setMargin(true);
		vl.addComponent(users);
		loadUsers();

		setCompositionRoot(vl);
	}

	private void loadUsers() {
		users.removeAllComponents();
		for (final String user : DatabaseService.listUsernames()) {
			HorizontalLayout hl = new HorizontalLayout();
			if (user.equals(ConfigurationService.getProperty("adminUser"))) {
				hl.addComponent(new Label(user + " [admin]"));
			} else {
				hl.addComponent(new Label(user));
				hl.addComponent(new Button("Delete", new ClickListener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void buttonClick(ClickEvent event) {
						DatabaseService.removeUser(user);
						loadUsers();
					}
				}));
			}
			users.addComponent(hl);
			users.setImmediate(true);
		}
	}

	@Override
	public void enter(ViewChangeEvent event) {
		String username = String.valueOf(getSession().getAttribute("user"));
		header.enableLogout();
		text.setValue("Current user: " + username);

		btnCreateUser.addClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				CreateUserWindow uv = new CreateUserWindow();
				UI.getCurrent().addWindow(uv);
				uv.addCloseListener(new Window.CloseListener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void windowClose(CloseEvent e) {
						loadUsers();
					}
				});

			}
		});

		backButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				getUI().getNavigator().navigateTo(MainView.NAME);
			}
		});
	}

}