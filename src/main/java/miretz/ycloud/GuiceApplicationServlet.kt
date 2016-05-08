package miretz.ycloud

import com.google.inject.Inject
import com.google.inject.Singleton
import com.vaadin.annotations.VaadinServletConfiguration
import com.vaadin.server.SessionInitEvent
import com.vaadin.server.SessionInitListener
import com.vaadin.server.VaadinServlet

@SuppressWarnings("serial")
@Singleton
@VaadinServletConfiguration(productionMode = false, ui = YCloudUI::class, widgetset = "miretz.ycloud.YCloudWidgetset") class GuiceApplicationServlet
@Inject
constructor(protected val applicationProvider: GuiceUIProvider) : VaadinServlet(), SessionInitListener {

    override fun servletInitialized() {
        service.addSessionInitListener(this)
    }

    override fun sessionInit(event: SessionInitEvent) {
        event.session.addUIProvider(applicationProvider)
    }
}