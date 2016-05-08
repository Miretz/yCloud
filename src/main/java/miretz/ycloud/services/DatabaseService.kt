package miretz.ycloud.services


import miretz.ycloud.models.Document

interface DatabaseService {

    /** user interaction  */

    fun checkUserPassword(username: String, password: String): Boolean

    fun addUser(username: String, password: String)

    fun removeUser(username: String)

    fun listUsernames(): List<String>

    /** file interaction  */

    fun addDocument(documentToStore: Document)

    fun getAllDocuments(): List<Document>

    fun getDescendants(parentId: String): List<Document>

    fun findDocument(contentId: String): Document

    fun findByMetadataValue(key: String, value: String): Document

    fun deleteDocument(contentId: String)

    fun deleteAllDocuments()

}
