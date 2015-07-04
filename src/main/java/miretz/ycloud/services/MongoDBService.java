package miretz.ycloud.services;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Singleton;

import miretz.ycloud.services.utils.PasswordHashUtil;

import org.bson.Document;

import com.github.fakemongo.Fongo;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@Singleton
public class MongoDBService implements DatabaseService {

	private static final String PROPERTY_FILES_DB_TABLE_NAME = "filesDbTableName";
	private static final String PROPERTY_USERS_DB_TABLE_NAME = "usersDbTableName";
	private static final String PROPERTY_ADMIN_USER = "adminUser";
	private static final String PROPERTY_ADMIN_PASS = "adminPass";
	protected static final String PROPERTY_DB_HOST = "dbHost";
	protected static final String PROPERTY_DB_PASS = "dbPass";
	protected static final String PROPERTY_DB_USER = "dbUser";
	protected static final String PROPERTY_USE_FAKE_MONGO = "useFakeMongo";
	protected static final String PROPERTY_DB_NAME = "dbName";

	protected MongoClient mongo;
	protected MongoDatabase database;

	protected String adminPass;
	protected String adminUser;
	protected String usersDbTableName;
	protected String filesDbTableName;

	@Inject
	public MongoDBService(@Named(PROPERTY_USE_FAKE_MONGO) String useFakeMongo, @Named(PROPERTY_DB_NAME) String dbName, @Named(PROPERTY_DB_HOST) String dbHost, @Named(PROPERTY_DB_PASS) String dbPass, @Named(PROPERTY_DB_USER) String dbUser, @Named(PROPERTY_ADMIN_USER) String adminUser,
			@Named(PROPERTY_ADMIN_PASS) String adminPass, @Named(PROPERTY_USERS_DB_TABLE_NAME) String usersDbTableName, @Named(PROPERTY_FILES_DB_TABLE_NAME) String filesDbTableName) {

		this.adminPass = adminPass;
		this.adminUser = adminUser;
		this.usersDbTableName = usersDbTableName;
		this.filesDbTableName = filesDbTableName;

		if (Boolean.parseBoolean(useFakeMongo)) {

			// fake mongo
			Fongo fongo = new Fongo("fake mongo server");
			database = fongo.getDatabase(dbName);

		} else {

			// Real mongo
			MongoCredential credential = MongoCredential.createMongoCRCredential(dbUser, dbName, dbPass.toCharArray());
			mongo = new MongoClient(new ServerAddress(dbHost), Arrays.asList(credential));
			database = mongo.getDatabase(dbName);

		}

		adminUserSetup();
		rootFolderSetup();
	}

	public void rootFolderSetup() {
		addDocument(new miretz.ycloud.models.Document("root", "root", "", new HashMap<String, String>(), miretz.ycloud.models.Document.TYPE_FOLDER));
	}

	public void adminUserSetup() {

		// drop all users
		// database.getCollection(USERS_TABLE).drop();

		// remove admin
		removeUser(adminUser);

		MongoCollection<Document> table = database.getCollection(usersDbTableName);

		Document document = new Document();
		document.put("username", adminUser);
		document.put("password", PasswordHashUtil.createHash(adminPass));
		document.put("createdDate", new Date());

		table.insertOne(document);
	}

	@Override
	public boolean checkUserPassword(String username, String password) {

		MongoCollection<Document> table = database.getCollection(usersDbTableName);
		FindIterable<Document> users = table.find(eq("username", username));

		for (Document user : users) {
			String pValue = user.getString("password");
			if (PasswordHashUtil.validatePassword(password, pValue)) {
				return true;
			}
		}

		return false;

	}

	@Override
	public void addUser(String username, String password) {
		String hash = PasswordHashUtil.createHash(password);
		if (hash != null) {
			MongoCollection<Document> table = database.getCollection(usersDbTableName);

			Document document = new Document();
			document.put("username", username);
			document.put("password", hash);
			document.put("createdDate", new Date());

			table.insertOne(document);
		}
	}

	@Override
	public void removeUser(String username) {
		MongoCollection<Document> table = database.getCollection(usersDbTableName);
		table.deleteOne(eq("username", username));
	}

	@Override
	public List<String> listUsernames() {
		MongoCollection<Document> table = database.getCollection(usersDbTableName);

		List<String> result = new ArrayList<String>();

		for (Document cur : table.find()) {
			result.add(cur.getString("username"));
		}

		return result;
	}

	/**
	 * DOCUMENT METHODS
	 */

	@Override
	public void deleteDocument(String contentId) {

		MongoCollection<Document> table = database.getCollection(filesDbTableName);

		// delete all children
		table.deleteMany(eq("parentId", contentId));

		// delete the document
		table.deleteOne(eq("contentId", contentId));

	}

	@Override
	public void addDocument(miretz.ycloud.models.Document documentToStore) {
		MongoCollection<Document> table = database.getCollection(filesDbTableName);

		Document document = new Document();
		document.append("contentId", documentToStore.getContentId());
		document.append("fileName", documentToStore.getFileName());
		document.append("parentId", documentToStore.getParentId());
		document.append("type", documentToStore.getType());

		Document metadata = new Document();
		for (Entry<String, String> entry : documentToStore.getMetadata().entrySet()) {
			metadata.append(entry.getKey(), entry.getValue());
		}
		document.append("metadata", metadata);
		document.append("createdDate", new Date());

		table.insertOne(document);

	}

	@Override
	public List<miretz.ycloud.models.Document> getAllDocuments() {
		MongoCollection<Document> table = database.getCollection(filesDbTableName);
		List<miretz.ycloud.models.Document> result = new ArrayList<>();

		for (Document document : table.find()) {
			result.add(getDocument(document));
		}

		return result;
	}

	@Override
	public List<miretz.ycloud.models.Document> getDescendants(String parentId) {
		MongoCollection<Document> table = database.getCollection(filesDbTableName);

		List<miretz.ycloud.models.Document> result = new ArrayList<>();

		FindIterable<Document> myDoc = table.find(eq("parentId", parentId));

		if (myDoc == null) {
			return result;
		}

		for (Document document : myDoc) {
			result.add(getDocument(document));
		}

		return result;

	}

	@Override
	public miretz.ycloud.models.Document getDocument(String contentId) {

		MongoCollection<Document> table = database.getCollection(filesDbTableName);

		Document document = table.find(eq("contentId", contentId)).first();

		if (document != null) {
			return getDocument(document);

		}

		return null;

	}

	private Map<String, String> getMetadataAsMap(Document document) {
		Map<String, String> metadataMap = new HashMap<>();
		for (Entry<String, Object> entry : document.get("metadata", Document.class).entrySet()) {
			metadataMap.put(entry.getKey(), (String) entry.getValue());
		}
		return metadataMap;
	}

	@Override
	public miretz.ycloud.models.Document findByMetadataValue(String key, String value) {
		MongoCollection<Document> table = database.getCollection(filesDbTableName);

		Document document = table.find(eq(key, value)).first();

		if (document != null) {
			return getDocument(document);
		}

		return null;
	}

	private miretz.ycloud.models.Document getDocument(Document document) {
		return new miretz.ycloud.models.Document(document.getString("contentId"), document.getString("fileName"), document.getString("parentId"), getMetadataAsMap(document), document.getString("type"));
	}

	@Override
	public void deleteAllDocuments() {
		MongoCollection<Document> table = database.getCollection(filesDbTableName);
		table.drop();
	}
}
