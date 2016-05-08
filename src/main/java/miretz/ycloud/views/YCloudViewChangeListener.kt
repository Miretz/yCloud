package miretz.ycloud.views

import com.vaadin.navigator.ViewChangeListener
import com.vaadin.ui.UI

class YCloudViewChangeListener(protected var mainUi: UI) : ViewChangeListener {

    override fun beforeViewChange(event: ViewChangeListener.ViewChangeEvent): Boolean {

        val isLoggedIn = mainUi.session.getAttribute("user") != null
        val isLoginView = event.newView is LoginView

        if (!isLoggedIn && !isLoginView) {
            mainUi.navigator.navigateTo(LoginView.NAME)
            return false
        } else if (isLoggedIn && isLoginView) {
            return false
        }
        return true
    }

    override fun afterViewChange(event: ViewChangeListener.ViewChangeEvent) {
    }
}
