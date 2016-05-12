package miretz.ycloud.views.partials

import com.vaadin.server.Sizeable
import com.vaadin.server.ThemeResource
import com.vaadin.ui.Button
import com.vaadin.ui.Button.ClickEvent
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.Image
import com.vaadin.ui.Label
import com.vaadin.ui.themes.ValoTheme
import miretz.ycloud.views.LoginView
import miretz.ycloud.views.UsersView

class HeaderPanel : HorizontalLayout() {
    private val loginName: Button
    private val users: Button

    init {
        styleName = "backBlue"
        setWidth(100f, Sizeable.Unit.PERCENTAGE)

        val resource = ThemeResource("img/cloud.png")
        val image = Image(null, resource)
        addComponent(image)

        val title = Label("yCloud")
        title.addStyleName("title")
        title.setWidth(100f, Sizeable.Unit.PERCENTAGE)
        addComponent(title)

        // users button
        users = Button("Users", Button.ClickListener { ui.navigator.navigateTo(UsersView.NAME) })
        users.setWidth(null)
        users.isEnabled = false
        users.isVisible = false
        users.styleName = ValoTheme.BUTTON_BORDERLESS
        addComponent(users)

        // logout button
        loginName = Button("Logout", Button.ClickListener {
            session.setAttribute("user", null)
            ui.navigator.navigateTo(LoginView.NAME)
        })
        loginName.setWidth(null)
        loginName.isEnabled = false
        loginName.isVisible = false
        loginName.styleName = ValoTheme.BUTTON_BORDERLESS
        loginName.icon = ThemeResource("img/logout.png")
        addComponent(loginName)

        setExpandRatio(title, 1.0f)

        isImmediate = true
    }

    fun enableLogout() {
        loginName.isVisible = true
        loginName.isEnabled = true
        loginName.isImmediate = true
        isImmediate = true
    }

    fun enableUsers() {
        users.isVisible = true
        users.isEnabled = true
        users.isImmediate = true
        isImmediate = true
    }
}
