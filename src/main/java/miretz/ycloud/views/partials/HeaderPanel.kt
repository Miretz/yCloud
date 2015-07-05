package miretz.ycloud.views.partials

import com.vaadin.server.Sizeable
import miretz.ycloud.views.LoginView
import miretz.ycloud.views.UsersView

import com.vaadin.server.ThemeResource
import com.vaadin.ui.Button
import com.vaadin.ui.Button.ClickEvent
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.Image
import com.vaadin.ui.Label
import com.vaadin.ui.themes.ValoTheme

public class HeaderPanel : HorizontalLayout() {
    private val loginName: Button
    private val users: Button

    init {
        setStyleName("backBlue")
        setWidth(100f, Sizeable.Unit.PERCENTAGE)

        val resource = ThemeResource("img/cloud.png")
        val image = Image(null, resource)
        addComponent(image)

        val title = Label("yCloud")
        title.addStyleName("title")
        title.setWidth(100f, Sizeable.Unit.PERCENTAGE)
        addComponent(title)

        // users button
        users = Button("Users", object : Button.ClickListener {

            override fun buttonClick(event: ClickEvent) {
                getUI().getNavigator().navigateTo(UsersView.NAME)
            }
        })
        users.setWidth(null)
        users.setEnabled(false)
        users.setVisible(false)
        users.setStyleName(ValoTheme.BUTTON_BORDERLESS)
        addComponent(users)

        // logout button
        loginName = Button("Logout", object : Button.ClickListener {

            override fun buttonClick(event: ClickEvent) {
                getSession().setAttribute("user", null)
                getUI().getNavigator().navigateTo(LoginView.NAME)
            }
        })
        loginName.setWidth(null)
        loginName.setEnabled(false)
        loginName.setVisible(false)
        loginName.setStyleName(ValoTheme.BUTTON_BORDERLESS)
        loginName.setIcon(ThemeResource("img/logout.png"))
        addComponent(loginName)

        setExpandRatio(title, 1.0f)

        setImmediate(true)
    }

    public fun enableLogout() {
        loginName.setVisible(true)
        loginName.setEnabled(true)
        loginName.setImmediate(true)
        setImmediate(true)
    }

    public fun enableUsers() {
        users.setVisible(true)
        users.setEnabled(true)
        users.setImmediate(true)
        setImmediate(true)
    }
}
