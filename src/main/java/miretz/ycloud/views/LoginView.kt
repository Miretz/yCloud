package miretz.ycloud.views

import miretz.ycloud.services.DatabaseService
import miretz.ycloud.services.utils.PasswordValidator
import miretz.ycloud.views.partials.HeaderPanel

import com.google.inject.Inject
import com.vaadin.data.validator.EmailValidator
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.vaadin.shared.ui.MarginInfo
import com.vaadin.ui.Alignment
import com.vaadin.ui.Button
import com.vaadin.ui.Button.ClickEvent
import com.vaadin.ui.CustomComponent
import com.vaadin.ui.PasswordField
import com.vaadin.ui.TextField
import com.vaadin.ui.VerticalLayout

public class LoginView
@Inject
constructor(protected var databaseService: DatabaseService) : CustomComponent(), View, Button.ClickListener {

    private val user: TextField
    private val password: PasswordField
    private val loginButton: Button

    init {
        this.user = TextField("User:")
        this.password = PasswordField("Password:")
        this.loginButton = Button("Login", this)
    }

    public fun initialize() {
        setSizeFull()

        // Create the user input field
        user.setWidth("300px")
        user.setRequired(true)
        user.setInputPrompt("Your username (eg. joe@email.com)")
        user.addValidator(EmailValidator("Username must be an email address"))
        user.setInvalidAllowed(false)

        // Create the password input field
        password.setWidth("300px")
        password.addValidator(PasswordValidator())
        password.setRequired(true)
        password.setValue("")
        password.setNullRepresentation("")

        // Add both to a panel
        val fields = VerticalLayout(user, password, loginButton)
        fields.setSpacing(true)
        fields.setMargin(MarginInfo(true, true, true, false))
        fields.setSizeUndefined()

        // The view root layout
        val viewLayout = VerticalLayout(fields)
        viewLayout.setSizeFull()
        viewLayout.setComponentAlignment(fields, Alignment.MIDDLE_CENTER)

        val mainLayout = VerticalLayout(HeaderPanel(), viewLayout)
        setCompositionRoot(mainLayout)
    }

    override fun enter(event: ViewChangeEvent) {
        initialize()
        user.focus()
    }

    override fun buttonClick(event: ClickEvent) {

        if (!user.isValid() || !password.isValid()) {
            return
        }

        val username = user.getValue()

        val isValid = databaseService.checkUserPassword(username, password.getValue())

        if (isValid) {
            getSession().setAttribute("user", username)
            getUI().getNavigator().navigateTo(MainView.NAME)
        } else {
            this.password.setValue(null)
            this.password.focus()
        }
    }

    companion object {
        public val NAME: String = "login"
    }
}