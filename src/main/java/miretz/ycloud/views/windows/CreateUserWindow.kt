package miretz.ycloud.views.windows

import com.vaadin.data.validator.EmailValidator
import com.vaadin.ui.*
import miretz.ycloud.services.DatabaseService
import miretz.ycloud.services.utils.PasswordValidator

class CreateUserWindow(databaseService: DatabaseService) : Window("Create User") {

    init {

        center()

        // Some basic content for the window
        val content = VerticalLayout()
        content.setMargin(true)
        content.isSpacing = true
        setContent(content)

        // Disable the close button
        isClosable = true
        isResizable = false
        isModal = true

        // Create the user input field
        val fldUsername: TextField = TextField("User:")
        fldUsername.setWidth("300px")
        fldUsername.isRequired = true
        fldUsername.inputPrompt = "Your username (eg. joe@email.com)"
        fldUsername.addValidator(EmailValidator("Username must be an email address"))
        fldUsername.isInvalidAllowed = false
        content.addComponent(fldUsername)

        // Create the password input field
        val fldPassword: PasswordField = PasswordField("Password:")
        fldPassword.setWidth("300px")
        fldPassword.addValidator(PasswordValidator())
        fldPassword.isRequired = true
        fldPassword.value = ""
        fldPassword.nullRepresentation = ""
        content.addComponent(fldPassword)

        val btnCreateUser: Button = Button("Create User")
        btnCreateUser.addClickListener {
            if (!fldUsername.isValid || !fldPassword.isValid) {
                fldUsername.value = "Username not valid!"
                fldPassword.value = null
                fldUsername.focus()
            } else {
                val username = fldUsername.value
                val password = fldPassword.value
                databaseService.addUser(username, password)
                close()
            }
        }
        content.addComponent(btnCreateUser)

        fldUsername.focus()

    }

}
