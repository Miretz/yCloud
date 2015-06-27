package miretz.ycloud;

import miretz.ycloud.views.LoginView;
import miretz.ycloud.views.MainView;
import miretz.ycloud.views.UsersView;
import miretz.ycloud.views.YCloudViewChangeListener;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

/**
 *
 */
@SuppressWarnings("serial")
@Theme("ycloud")
public class YCloudUI extends UI {

	@Inject
	private Injector injector;

	@Override
	protected void init(VaadinRequest vaadinRequest) {
		new Navigator(this, this);

		getNavigator().addView(LoginView.NAME, injector.getInstance(LoginView.class));
		getNavigator().addView(MainView.NAME, injector.getInstance(MainView.class));
		getNavigator().addView(UsersView.NAME, injector.getInstance(UsersView.class));
		getNavigator().addViewChangeListener(new YCloudViewChangeListener(this));

		UI.getCurrent().setErrorHandler(new DefaultErrorHandler() {
			@Override
			public void error(com.vaadin.server.ErrorEvent event) {
				// Find the final cause
				String cause = "Application Error: ";
				
				for (Throwable t = event.getThrowable(); t != null; t = t.getCause())
					if (t.getCause() == null) // We're at final cause
						cause += t.getClass().getName();

				Notification.show("Error", cause, Notification.Type.ERROR_MESSAGE);

				// Do the default error handling (optional)
				doDefault(event);
			}
		});

	}
}
