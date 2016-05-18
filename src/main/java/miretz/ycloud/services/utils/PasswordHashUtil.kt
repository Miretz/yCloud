package miretz.ycloud.services.utils

import org.mindrot.jbcrypt.BCrypt

class PasswordHashUtil {

    fun createHash(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt(12))
    }

    fun validatePassword(password: String, hashedPasswordFromDb: String): Boolean {
        return BCrypt.checkpw(password, hashedPasswordFromDb)
    }

}
