package miretz.ycloud.views.windows

import miretz.ycloud.services.DatabaseService
import miretz.ycloud.services.utils.PasswordValidator

import com.vaadin.data.validator.EmailValidator
import com.vaadin.ui.Button
import com.vaadin.ui.Button.ClickEvent
import com.vaadin.ui.PasswordField
import com.vaadin.ui.TextField
import com.vaadin.ui.VerticalLayout
import com.vaadin.ui.Window

public class CreateUserWindow(databaseService: DatabaseService) : Window("Create User") {

    private val fldUsername: TextField
    private val fldPassword: PasswordField
    private val btnCreateUser: Button

    init {

        center()

        // Some basic content for the window
        val content = VerticalLayout()
        content.setMargin(true)
        content.setSpacing(true)
        setContent(content)

        // Disable the close button
        setClosable(true)
        setResizable(false)
        setModal(true)

        // Create the user input field
        fldUsername = TextField("User:")
        fldUsername.setWidth("300px")
        fldUsername.setRequired(true)
        fldUsername.setInputPrompt("Your username (eg. joe@email.com)")
        fldUsername.addValidator(EmailValidator("Username must be an email address"))
        fldUsername.setInvalidAllowed(false)
        content.addComponent(fldUsername)

        // Create the password input field
        fldPassword = PasswordField("Password:")
        fldPassword.setWidth("300px")
        fldPassword.addValidator(PasswordValidator())
        fldPassword.setRequired(true)
        fldPassword.setValue("")
        fldPassword.setNullRepresentation("")
        content.addComponent(fldPassword)

        btnCreateUser = Button("Create User")
        btnCreateUser.addClickListener(object : Button.ClickListener {

            override fun buttonClick(event: ClickEvent) {
                if (!fldUsername.isValid() || !fldPassword.isValid()) {
                    fldUsername.setValue("Username not valid!")
                    fldPassword.setValue(null)
                    fldUsername.focus()
                } else {
                    val username = fldUsername.getValue()
                    val password = fldPassword.getValue()
                    databaseService.addUser(username, password)
                    close()
                }
            }

        })
        content.addComponent(btnCreateUser)

        fldUsername.focus()

    }

}
