package miretz.ycloud.services


import miretz.ycloud.models.Document

public interface DatabaseService {

    /** user interaction  */

    public fun checkUserPassword(username: String, password: String): Boolean

    public fun addUser(username: String, password: String)

    public fun removeUser(username: String)

    public fun listUsernames(): List<String>

    /** file interaction  */

    public fun addDocument(documentToStore: Document)

    public fun getAllDocuments(): List<Document>

    public fun getDescendants(parentId: String): List<Document>

    public fun findDocument(contentId: String): Document

    public fun findByMetadataValue(key: String, value: String): Document

    public fun deleteDocument(contentId: String)

    public fun deleteAllDocuments()

}
