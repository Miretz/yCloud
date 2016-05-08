package miretz.ycloud

import com.google.inject.Inject
import com.google.inject.Injector
import com.vaadin.server.UIClassSelectionEvent
import com.vaadin.server.UICreateEvent
import com.vaadin.server.UIProvider

@SuppressWarnings("serial") class GuiceUIProvider : UIProvider() {

    @Inject
    private val injector: Injector? = null

    override fun createInstance(event: UICreateEvent): YCloudUI {
        return injector!!.getInstance(YCloudUI::class.java)
    }

    override fun getUIClass(event: UIClassSelectionEvent): Class<YCloudUI> {
        return YCloudUI::class.java
    }
}
