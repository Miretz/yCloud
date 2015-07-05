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

public class UsersView
@Inject
constructor(Named("adminUser") protected var adminUser: String, protected var databaseService: DatabaseService) : CustomComponent(), View {

    private var text: Label? = null
    private var header: HeaderPanel? = null
    private var btnCreateUser: Button? = null
    private var backButton: Button? = null
    private var users: VerticalLayout? = null


    public fun initialize() {

        text = Label()
        header = HeaderPanel()
        btnCreateUser = Button("Create User")
        backButton = Button("Return")
        users = VerticalLayout()

        val vl = VerticalLayout()
        vl.setSpacing(true)
        vl.setSizeFull()
        vl.addComponent(header)

        val statistics = HorizontalLayout()
        statistics.addComponent(text)
        statistics.setSpacing(true)

        vl.addComponent(statistics)
        vl.setComponentAlignment(statistics, Alignment.MIDDLE_CENTER)

        val buttons = HorizontalLayout(btnCreateUser, backButton)
        vl.addComponent(buttons)
        vl.setComponentAlignment(buttons, Alignment.MIDDLE_RIGHT)
        users!!.setMargin(true)
        vl.addComponent(users)
        loadUsers()

        setCompositionRoot(vl)
    }

    private fun loadUsers() {
        users!!.removeAllComponents()
        for (user in databaseService.listUsernames()) {
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
            users!!.addComponent(hl)
            users!!.setImmediate(true)
        }
    }

    override fun enter(event: ViewChangeEvent) {

        initialize()

        val username = (getSession().getAttribute("user")) as String
        header!!.enableLogout()
        text!!.setValue("Current user: " + username)

        btnCreateUser!!.addClickListener(object : Button.ClickListener {

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

        backButton!!.addClickListener(object : Button.ClickListener {

            override fun buttonClick(event: ClickEvent) {
                getUI().getNavigator().navigateTo(MainView.NAME)
            }
        })
    }

    companion object {
        public val NAME: String = "users"
    }

}