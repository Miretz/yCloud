package miretz.ycloud.dbconnectors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import miretz.ycloud.services.ConfigurationService;
import miretz.ycloud.services.PasswordHashService;

import com.github.fakemongo.Fongo;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class MongoDbConnector {

	private static final String USERS_TABLE = "users";
	private static final String FILES_TABLE = "files";
	private static MongoClient mongo;
	private static DB database;

	static {
		String dbUser = ConfigurationService.getProperty("dbUser");
		String dbPass = ConfigurationService.getProperty("dbPass");
		String dbName = ConfigurationService.getProperty("dbName");
		String dbHost = ConfigurationService.getProperty("dbHost");

		boolean useFakeMongo = ConfigurationService.getBooleanProperty("useFakeMongo");

		if (useFakeMongo) {

			// fake mongo
			Fongo fongo = new Fongo("mongo server 1");
			database = fongo.getDB(dbName);

		} else {

			// Real mongo
			MongoCredential credential = MongoCredential.createMongoCRCredential(dbUser, dbName, dbPass.toCharArray());
			mongo = new MongoClient(new ServerAddress(dbHost), Arrays.asList(credential));
			database = mongo.getDB(dbName);

		}

		adminUserSetup();

	}

	public static void adminUserSetup() {
		String username = ConfigurationService.getProperty("adminUser");
		String password = ConfigurationService.getProperty("adminPass");

		// drop all users
		// database.getCollection(USERS_TABLE).drop();

		// remove admin
		removeUser(username);

		DBCollection table = database.getCollection(USERS_TABLE);
		BasicDBObject document = new BasicDBObject();
		document.put("username", username);
		document.put("password", PasswordHashService.createHash(password));
		document.put("createdDate", new Date());
		table.insert(document);
	}

	public static boolean checkUserPassword(String username, String password) {
		DBCollection table = database.getCollection(USERS_TABLE);
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("username", username);
		DBCursor cursor = table.find(searchQuery);
		if (cursor.count() < 1) {
			return false;
		}
		while (cursor.hasNext()) {
			String pValue = (String) cursor.next().get("password");
			if (PasswordHashService.validatePassword(password, pValue)) {
				return true;
			}
		}
		return false;

	}

	public static void addUser(String username, String password) {
		String hash = PasswordHashService.createHash(password);
		if (hash != null) {
			DBCollection table = database.getCollection(USERS_TABLE);
			BasicDBObject document = new BasicDBObject();
			document.put("username", username);
			document.put("password", hash);
			document.put("createdDate", new Date());
			table.insert(document);
		}
	}

	public static void removeUser(String username) {
		DBCollection table = database.getCollection(USERS_TABLE);
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("username", username);
		table.remove(searchQuery);
	}

	public static List<String> listUsernames() {
		DBCollection table = database.getCollection(USERS_TABLE);
		List<String> result = new ArrayList<String>();
		DBCursor cursor = table.find();
		while (cursor.hasNext()) {
			result.add((String) (cursor.next()).get("username"));
		}
		return result;
	}

	public static void addFile(String fileName, String comment, String creator) {
		DBCollection table = database.getCollection(FILES_TABLE);
		BasicDBObject document = new BasicDBObject();
		document.put("fileName", fileName);
		document.put("comment", comment);
		document.put("creator", creator);
		document.put("createdDate", new Date());
		table.insert(document);
	}

	public static void deleteFile(String fileName) {
		DBCollection table = database.getCollection(FILES_TABLE);
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("fileName", fileName);
		table.remove(searchQuery);
	}

	public static String getFileParameter(String fileName, String parameter) {
		DBCollection table = database.getCollection(FILES_TABLE);
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("fileName", fileName);
		DBCursor cursor = table.find(searchQuery);
		String value = "";
		if (cursor.count() < 1) {
			return value;
		}
		while (cursor.hasNext()) {
			value = (String) cursor.next().get(parameter);
		}
		return value;

	}

}
