package miretz.ycloud.views

import miretz.ycloud.services.DatabaseService
import miretz.ycloud.views.partials.HeaderPanel
import miretz.ycloud.views.windows.CreateUserWindow

import com.google.inject.Inject
import com.google.inject.name.Named
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.vaadin.ui.Alignment
import com.vaadin.ui.Button
import com.vaadin.ui.Button.ClickEvent
import com.vaadin.ui.Button.ClickListener
import com.vaadin.ui.CustomComponent
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.Label
import com.vaadin.ui.UI
import com.vaadin.ui.VerticalLayout
import com.vaadin.ui.Window
import com.vaadin.ui.Window.CloseEvent

class UsersView
@Inject
constructor(@Named("adminUser") protected var adminUser: String, protected var databaseService: DatabaseService) : CustomComponent(), View {

    private val text: Label = Label()
    private val header: HeaderPanel = HeaderPanel()
    private val btnCreateUser: Button = Button("Create User")
    private val backButton: Button = Button("Return")
    private val users: VerticalLayout = VerticalLayout()

    fun initialize() {

        val vl = VerticalLayout()
        vl.isSpacing = true
        vl.setSizeFull()
        vl.addComponent(header)

        val statistics = HorizontalLayout()
        statistics.addComponent(text)
        statistics.isSpacing = true

        vl.addComponent(statistics)
        vl.setComponentAlignment(statistics, Alignment.MIDDLE_CENTER)

        val buttons = HorizontalLayout(btnCreateUser, backButton)
        vl.addComponent(buttons)
        vl.setComponentAlignment(buttons, Alignment.MIDDLE_RIGHT)
        users.setMargin(true)
        vl.addComponent(users)
        loadUsers()

        compositionRoot = vl
    }

    private fun loadUsers() {
        users.removeAllComponents()
        databaseService.listUsernames().forEach { user ->
            val hl = HorizontalLayout()
            if (user == adminUser) {
                hl.addComponent(Label(user + " [admin]"))
            } else {
                hl.addComponent(Label(user))
                hl.addComponent(Button("Delete", object : ClickListener {

                    override fun buttonClick(event: ClickEvent) {
                        databaseService.removeUser(user)
                        loadUsers()
                    }
                }))
            }
            users.addComponent(hl)
            users.isImmediate = true
        }
    }

    override fun enter(event: ViewChangeEvent) {

        initialize()

        val username = (session.getAttribute("user")) as String
        header.enableLogout()
        text.value = "Current user: " + username

        btnCreateUser.addClickListener(object : Button.ClickListener {

            override fun buttonClick(event: ClickEvent) {
                val uv = CreateUserWindow(databaseService)
                UI.getCurrent().addWindow(uv)
                uv.addCloseListener(object : Window.CloseListener {

                    override fun windowClose(e: CloseEvent) {
                        loadUsers()
                    }
                })

            }
        })

        backButton.addClickListener(object : Button.ClickListener {

            override fun buttonClick(event: ClickEvent) {
                ui.navigator.navigateTo(MainView.NAME)
            }
        })
    }

    companion object {
        val NAME: String = "users"
    }

}