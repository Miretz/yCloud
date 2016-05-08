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

class LoginView
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

    fun initialize() {
        setSizeFull()

        // Create the user input field
        user.setWidth("300px")
        user.isRequired = true
        user.inputPrompt = "Your username (eg. joe@email.com)"
        user.addValidator(EmailValidator("Username must be an email address"))
        user.isInvalidAllowed = false

        // Create the password input field
        password.setWidth("300px")
        password.addValidator(PasswordValidator())
        password.isRequired = true
        password.value = ""
        password.nullRepresentation = ""

        // Add both to a panel
        val fields = VerticalLayout(user, password, loginButton)
        fields.isSpacing = true
        fields.margin = MarginInfo(true, true, true, false)
        fields.setSizeUndefined()

        // The view root layout
        val viewLayout = VerticalLayout(fields)
        viewLayout.setSizeFull()
        viewLayout.setComponentAlignment(fields, Alignment.MIDDLE_CENTER)

        val mainLayout = VerticalLayout(HeaderPanel(), viewLayout)
        compositionRoot = mainLayout
    }

    override fun enter(event: ViewChangeEvent) {
        initialize()
        user.focus()
    }

    override fun buttonClick(event: ClickEvent) {

        if (!user.isValid || !password.isValid) {
            return
        }

        val username = user.value

        val isValid = databaseService.checkUserPassword(username, password.value)

        if (isValid) {
            session.setAttribute("user", username)
            ui.navigator.navigateTo(MainView.NAME)
        } else {
            this.password.value = null
            this.password.focus()
        }
    }

    companion object {
        val NAME: String = "login"
    }
}