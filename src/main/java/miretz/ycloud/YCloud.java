package miretz.ycloud;

import javax.servlet.annotation.WebServlet;

import miretz.ycloud.views.LoginView;
import miretz.ycloud.views.MainView;
import miretz.ycloud.views.UsersView;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

/**
 *
 */
@SuppressWarnings("serial")
@Theme("ycloud")
@Widgetset("miretz.ycloud.YCloudWidgetset")
public class YCloud extends UI {

	@Override
	protected void init(VaadinRequest vaadinRequest) {
		new Navigator(this, this);
		getNavigator().addView(LoginView.NAME, LoginView.class);
		getNavigator().addView(MainView.NAME, MainView.class);
		getNavigator().addView(UsersView.NAME, UsersView.class);

		getNavigator().addViewChangeListener(new ViewChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean beforeViewChange(ViewChangeEvent event) {

				boolean isLoggedIn = getSession().getAttribute("user") != null;
				boolean isLoginView = event.getNewView() instanceof LoginView;

				if (!isLoggedIn && !isLoginView) {
					getNavigator().navigateTo(LoginView.NAME);
					return false;
				} else if (isLoggedIn && isLoginView) {
					return false;
				}
				return true;
			}

			@Override
			public void afterViewChange(ViewChangeEvent event) {

			}
		});

		UI.getCurrent().setErrorHandler(new DefaultErrorHandler() {
			@Override
			public void error(com.vaadin.server.ErrorEvent event) {
				// Find the final cause
				String cause = "<b>The click failed because:</b><br/>";
				for (Throwable t = event.getThrowable(); t != null; t = t.getCause())
					if (t.getCause() == null) // We're at final cause
						cause += t.getClass().getName() + "<br/>";

				Notification.show("Error in application", cause, Notification.Type.WARNING_MESSAGE);

				// Do the default error handling (optional)
				doDefault(event);
			}
		});

	}

	@WebServlet(urlPatterns = "/*", name = "YCloudServlet", asyncSupported = true)
	@VaadinServletConfiguration(ui = YCloud.class, productionMode = false)
	public static class YCloudServlet extends VaadinServlet {
	}
}
