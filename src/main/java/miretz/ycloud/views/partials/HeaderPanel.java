package miretz.ycloud.views.partials;

import miretz.ycloud.views.LoginView;
import miretz.ycloud.views.UsersView;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;

public class HeaderPanel extends HorizontalLayout {

	private static final long serialVersionUID = 1L;
	private Button loginName;
	private Button users;

	public HeaderPanel() {

		super();
		setStyleName("backBlue");
		setWidth(100, Unit.PERCENTAGE);

		ThemeResource resource = new ThemeResource("img/cloud.png");
		Image image = new Image(null, resource);
		addComponent(image);

		Label title = new Label("yCloud");
		title.addStyleName("title");
		title.setWidth(100, Unit.PERCENTAGE);
		addComponent(title);

		// users button
		users = new Button("Users", new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				getUI().getNavigator().navigateTo(UsersView.NAME);
			}
		});
		users.setWidth(null);
		users.setEnabled(false);
		users.setVisible(false);
		users.setStyleName(ValoTheme.BUTTON_BORDERLESS);
		addComponent(users);

		// logout button
		loginName = new Button("Logout", new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				getSession().setAttribute("user", null);
				getUI().getNavigator().navigateTo(LoginView.NAME);
			}
		});
		loginName.setWidth(null);
		loginName.setEnabled(false);
		loginName.setVisible(false);
		loginName.setStyleName(ValoTheme.BUTTON_BORDERLESS);
		loginName.setIcon(new ThemeResource("img/logout.png"));
		addComponent(loginName);

		setExpandRatio(title, 1.0f);

		setImmediate(true);
	}

	public void enableLogout() {
		loginName.setVisible(true);
		loginName.setEnabled(true);
		loginName.setImmediate(true);
		setImmediate(true);
	}

	public void enableUsers() {
		users.setVisible(true);
		users.setEnabled(true);
		users.setImmediate(true);
		setImmediate(true);
	}

}
