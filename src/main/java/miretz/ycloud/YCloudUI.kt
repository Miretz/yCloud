package miretz.ycloud

import miretz.ycloud.views.LoginView
import miretz.ycloud.views.MainView
import miretz.ycloud.views.UsersView
import miretz.ycloud.views.YCloudViewChangeListener

import com.google.inject.Inject
import com.google.inject.Injector
import com.vaadin.annotations.Theme
import com.vaadin.navigator.Navigator
import com.vaadin.server.DefaultErrorHandler
import com.vaadin.server.VaadinRequest
import com.vaadin.ui.Notification
import com.vaadin.ui.UI

SuppressWarnings("serial")
Theme("ycloud")
public class YCloudUI : UI() {

    Inject
    private val injector: Injector? = null

    override fun init(vaadinRequest: VaadinRequest) {
        Navigator(this, this)

        getNavigator().addView(LoginView.NAME, injector!!.getInstance(javaClass<LoginView>()))
        getNavigator().addView(MainView.NAME, injector.getInstance(javaClass<MainView>()))
        getNavigator().addView(UsersView.NAME, injector.getInstance(javaClass<UsersView>()))
        getNavigator().addViewChangeListener(YCloudViewChangeListener(this))

        UI.getCurrent().setErrorHandler(object : DefaultErrorHandler() {
            override fun error(event: com.vaadin.server.ErrorEvent) {
                // Find the final cause
                var cause = "Application Error: "

                var t: Throwable? = event.getThrowable()
                while (t != null) {
                    if (t.getCause() == null)
                    // We're at final cause
                        cause += t.javaClass.getName()
                    t = t.getCause()
                }

                Notification.show("Error", cause, Notification.Type.ERROR_MESSAGE)

                // Do the default error handling (optional)
                DefaultErrorHandler.doDefault(event)
            }
        })

    }
}
