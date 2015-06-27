package miretz.ycloud;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.VaadinServlet;

@SuppressWarnings("serial")
@Singleton
@VaadinServletConfiguration(productionMode = false, ui = YCloudUI.class, widgetset = "miretz.ycloud.YCloudWidgetset")
public class GuiceApplicationServlet extends VaadinServlet implements SessionInitListener {
	
	protected final GuiceUIProvider applicationProvider;

	@Inject
	public GuiceApplicationServlet(GuiceUIProvider applicationProvider) {
		this.applicationProvider = applicationProvider;
	}

	@Override
	protected void servletInitialized() {
		getService().addSessionInitListener(this);
	}

	@Override
	public void sessionInit(SessionInitEvent event) throws ServiceException {
		event.getSession().addUIProvider(applicationProvider);
	}
}