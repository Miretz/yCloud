package miretz.ycloud.services

import com.github.fakemongo.Fongo
import com.google.inject.Inject
import com.google.inject.name.Named
import com.mongodb.MongoClient
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters.eq
import miretz.ycloud.services.utils.PasswordHashUtil
import org.bson.Document
import java.util.*
import javax.inject.Singleton

@Singleton
class MongoDBService
@Inject
constructor(
        @Named(MongoDBService.PROPERTY_USE_FAKE_MONGO) useFakeMongo: String,
        @Named(MongoDBService.PROPERTY_DB_NAME) dbName: String,
        @Named(MongoDBService.PROPERTY_DB_HOST) dbHost: String,
        @Named(MongoDBService.PROPERTY_DB_PASS) dbPass: String,
        @Named(MongoDBService.PROPERTY_DB_USER) dbUser: String,

        @Named("adminUser") protected var adminUser: String,
        @Named("adminPass") protected var adminPass: String,
        @Named("usersDbTableName") protected var usersDbTableName: String,
        @Named("filesDbTableName") protected var filesDbTableName: String) : DatabaseService {

    companion object {
        const val PROPERTY_DB_HOST: String = "dbHost"
        const val PROPERTY_DB_PASS: String = "dbPass"
        const val PROPERTY_DB_USER: String = "dbUser"
        const val PROPERTY_USE_FAKE_MONGO: String = "useFakeMongo"
        const val PROPERTY_DB_NAME: String = "dbName"
    }

    protected var database: MongoDatabase

    init {

        if (java.lang.Boolean.parseBoolean(useFakeMongo)) {

            // fake mongo
            val fongo = Fongo("fake mongo server")
            database = fongo.getDatabase(dbName)

        } else {

            // Real mongo
            val credential = MongoCredential.createMongoCRCredential(dbUser, dbName, dbPass.toCharArray())
            val mongo = MongoClient(ServerAddress(dbHost), Arrays.asList(credential))
            database = mongo.getDatabase(dbName)

        }

        adminUserSetup()
        rootFolderSetup()
    }

    fun rootFolderSetup() {
        addDocument(miretz.ycloud.models.Document("root", "root", "", HashMap<String, String>(), miretz.ycloud.models.Document.TYPE_FOLDER))
    }

    fun adminUserSetup() {

        // drop all users
        // database.getCollection(USERS_TABLE).drop();

        // remove admin
        removeUser(adminUser)

        val table = database.getCollection(usersDbTableName)
        val document = Document().append("username", adminUser).append("password", PasswordHashUtil.createHash(adminPass)).append("createdDate", Date())

        table.insertOne(document)
    }

    override fun checkUserPassword(username: String, password: String): Boolean {

        val table = database.getCollection(usersDbTableName)
        val users = table.find(eq("username", username))

        for (user in users) {
            val pValue = user.getString("password")
            if (PasswordHashUtil.validatePassword(password, pValue)) {
                return true
            }
        }

        return false

    }

    override fun addUser(username: String, password: String) {
        val hash = PasswordHashUtil.createHash(password) ?: IllegalStateException("Cannot hash password")
        val table = database.getCollection(usersDbTableName)
        val document = Document().append("username", username).append("password", hash).append("createdDate", Date())
        table.insertOne(document)
    }

    override fun removeUser(username: String) {
        val table = database.getCollection(usersDbTableName)
        table.deleteOne(eq("username", username))
    }

    override fun listUsernames(): List<String> {
        val table = database.getCollection(usersDbTableName)
        return table.find().map { it.getString("username") }.toList()
    }

    /**
     * DOCUMENT METHODS
     */

    override fun deleteDocument(contentId: String) {

        val table = database.getCollection(filesDbTableName)

        // delete all children
        table.deleteMany(eq("parentId", contentId))

        // delete the document
        table.deleteOne(eq("contentId", contentId))

    }

    override fun addDocument(documentToStore: miretz.ycloud.models.Document) {
        val table = database.getCollection(filesDbTableName)

        val document = Document()
        document.append("contentId", documentToStore.contentId)
        document.append("fileName", documentToStore.fileName)
        document.append("parentId", documentToStore.parentId)
        document.append("type", documentToStore.type)

        val metadata = Document()
        for (entry in documentToStore.metadata.entries) {
            metadata.append(entry.key, entry.value)
        }
        document.append("metadata", metadata)
        document.append("createdDate", Date())

        table.insertOne(document)

    }

    override fun getAllDocuments(): List<miretz.ycloud.models.Document> {
        val table = database.getCollection(filesDbTableName)
        val result : List<miretz.ycloud.models.Document> = table.find().map { it -> getDocument(it) }.toList()
        return result
    }

    override fun getDescendants(parentId: String): List<miretz.ycloud.models.Document> {
        val table = database.getCollection(filesDbTableName)
        val myDoc = table.find(eq<String>("parentId", parentId)) ?: return ArrayList()
        return myDoc.map { it -> getDocument(it) }.toList()
    }


    override fun findDocument(contentId: String): miretz.ycloud.models.Document {
        val table = database.getCollection(filesDbTableName)
        val document = table.find(eq("contentId", contentId)).first() ?: throw IllegalStateException("Document not found")
        return getDocument(document)
    }

    private fun getMetadataAsMap(document: Document): Map<String, String> {
        val metadataMap = HashMap<String, String>()
        for (entry in document.get("metadata", Document().javaClass).entries) {
            metadataMap.put(entry.key, entry.value as String)
        }
        return metadataMap
    }

    override fun findByMetadataValue(key: String, value: String): miretz.ycloud.models.Document {
        val table = database.getCollection(filesDbTableName)
        val document = table.find(eq(key, value)).first() ?: throw IllegalStateException("Document not found")
        return getDocument(document)
    }

    private fun getDocument(document: Document): miretz.ycloud.models.Document {
        return miretz.ycloud.models.Document(document.getString("contentId"), document.getString("fileName"), document.getString("parentId"), getMetadataAsMap(document), document.getString("type"))
    }

    override fun deleteAllDocuments() {
        val table = database.getCollection(filesDbTableName)
        table.drop()
    }

}
