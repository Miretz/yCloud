package miretz.ycloud.services;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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

	protected static final String PROPERTY_DB_HOST = "dbHost";
	protected static final String PROPERTY_DB_PASS = "dbPass";
	protected static final String PROPERTY_DB_USER = "dbUser";
	protected static final String PROPERTY_USE_FAKE_MONGO = "useFakeMongo";
	protected static final String PROPERTY_DB_NAME = "dbName";
	protected static final String USERS_TABLE = "users";
	protected static final String FILES_TABLE = "files";

	protected MongoClient mongo;
	protected MongoDatabase database;

	protected String adminPass;
	protected String adminUser;
	
	@Inject
	public MongoDBService(@Named(PROPERTY_USE_FAKE_MONGO) String useFakeMongo, @Named(PROPERTY_DB_NAME) String dbName, @Named(PROPERTY_DB_HOST) String dbHost, @Named(PROPERTY_DB_PASS) String dbPass, @Named(PROPERTY_DB_USER) String dbUser, @Named("adminUser") String adminUser,
			@Named("adminPass") String adminPass) {

		this.adminPass = adminPass;
		this.adminUser = adminUser;
		
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
	}

	public void adminUserSetup() {

		// drop all users
		// database.getCollection(USERS_TABLE).drop();

		// remove admin
		removeUser(adminUser);

		MongoCollection<Document> table = database.getCollection(USERS_TABLE);

		Document document = new Document();
		document.put("username", adminUser);
		document.put("password", PasswordHashUtil.createHash(adminPass));
		document.put("createdDate", new Date());

		table.insertOne(document);
	}

	public boolean checkUserPassword(String username, String password) {

		MongoCollection<Document> table = database.getCollection(USERS_TABLE);
		FindIterable<Document> users = table.find(eq("username", username));

		for (Document user : users) {
			String pValue = user.getString("password");
			if (PasswordHashUtil.validatePassword(password, pValue)) {
				return true;
			}
		}

		return false;

	}

	public void addUser(String username, String password) {
		String hash = PasswordHashUtil.createHash(password);
		if (hash != null) {
			MongoCollection<Document> table = database.getCollection(USERS_TABLE);

			Document document = new Document();
			document.put("username", username);
			document.put("password", hash);
			document.put("createdDate", new Date());

			table.insertOne(document);
		}
	}

	public void removeUser(String username) {
		MongoCollection<Document> table = database.getCollection(USERS_TABLE);
		table.deleteOne(eq("username", username));
	}

	public List<String> listUsernames() {
		MongoCollection<Document> table = database.getCollection(USERS_TABLE);

		List<String> result = new ArrayList<String>();

		for (Document cur : table.find()) {
			result.add(cur.getString("username"));
		}

		return result;
	}

	public void addFile(String fileName, String comment, String creator) {
		MongoCollection<Document> table = database.getCollection(FILES_TABLE);

		Document document = new Document();
		document.put("fileName", fileName);
		document.put("comment", comment);
		document.put("creator", creator);
		document.put("createdDate", new Date());

		table.insertOne(document);
	}

	public void deleteFile(String fileName) {
		MongoCollection<Document> table = database.getCollection(FILES_TABLE);
		table.deleteOne(eq("fileName", fileName));
	}

	public String getFileParameter(String fileName, String parameter) {
		MongoCollection<Document> table = database.getCollection(FILES_TABLE);
		Document myDoc = table.find(eq("fileName", fileName)).first();
		if (myDoc == null) {
			return "";
		}
		return myDoc.getString(parameter);
	}

	@Override
	public String getFileComment(String fileName) {
		return getFileParameter(fileName, "comment");
	}

	@Override
	public String getFileCreator(String fileName) {
		return getFileParameter(fileName, "creator");
	}

}
