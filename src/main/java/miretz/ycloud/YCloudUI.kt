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
import miretz.ycloud.retention.RetentionScheduler

@SuppressWarnings("serial")
@Theme("ycloud") class YCloudUI : UI() {

    @Inject
    private val injector: Injector? = null

    private var scheduler: RetentionScheduler? = null

    override fun init(vaadinRequest: VaadinRequest) {
        Navigator(this, this)

        navigator.addView(LoginView.NAME, injector!!.getInstance(LoginView::class.java))
        navigator.addView(MainView.NAME, injector.getInstance(MainView::class.java))
        navigator.addView(UsersView.NAME, injector.getInstance(UsersView::class.java))
        navigator.addViewChangeListener(YCloudViewChangeListener(this))

        scheduler = injector.getInstance(RetentionScheduler::class.java)

        UI.getCurrent().errorHandler = object : DefaultErrorHandler() {
            override fun error(event: com.vaadin.server.ErrorEvent) {
                // Find the final cause
                var cause = ""
                var t: Throwable? = event.throwable
                while (t != null) {
                    if (t.cause == null)
                    // We're at final cause
                        cause += t.message
                    t = t.cause
                }

                Notification.show("Error", cause, Notification.Type.ERROR_MESSAGE)

                // Do the default error handling (optional)
                DefaultErrorHandler.doDefault(event)
            }
        }

    }
}
