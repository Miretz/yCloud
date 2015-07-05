package miretz.ycloud

import com.google.inject.Inject
import com.google.inject.Singleton
import com.vaadin.annotations.VaadinServletConfiguration
import com.vaadin.server.ServiceException
import com.vaadin.server.SessionInitEvent
import com.vaadin.server.SessionInitListener
import com.vaadin.server.VaadinServlet

SuppressWarnings("serial")
Singleton
VaadinServletConfiguration(productionMode = false, ui = YCloudUI::class, widgetset = "miretz.ycloud.YCloudWidgetset")
public class GuiceApplicationServlet
@Inject
constructor(protected val applicationProvider: GuiceUIProvider) : VaadinServlet(), SessionInitListener {

    override fun servletInitialized() {
        getService().addSessionInitListener(this)
    }

    throws(ServiceException::class)
    override fun sessionInit(event: SessionInitEvent) {
        event.getSession().addUIProvider(applicationProvider)
    }
}