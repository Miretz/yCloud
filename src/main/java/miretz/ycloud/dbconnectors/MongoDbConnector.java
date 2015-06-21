package miretz.ycloud.dbconnectors;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import miretz.ycloud.services.ConfigurationService;
import miretz.ycloud.services.PasswordHashService;

import org.bson.Document;

import com.github.fakemongo.Fongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MongoDbConnector {

	private static final String USERS_TABLE = "users";
	private static final String FILES_TABLE = "files";
	private static MongoClient mongo;
	private static MongoDatabase database;

	static {
		String dbUser = ConfigurationService.getProperty("dbUser");
		String dbPass = ConfigurationService.getProperty("dbPass");
		String dbName = ConfigurationService.getProperty("dbName");
		String dbHost = ConfigurationService.getProperty("dbHost");

		boolean useFakeMongo = ConfigurationService.getBooleanProperty("useFakeMongo");

		if (useFakeMongo) {

			// fake mongo
			Fongo fongo = new Fongo("mongo server 1");
			database = fongo.getDatabase(dbName);

		} else {

			// Real mongo
			MongoCredential credential = MongoCredential.createMongoCRCredential(dbUser, dbName, dbPass.toCharArray());
			mongo = new MongoClient(new ServerAddress(dbHost), Arrays.asList(credential));
			database = mongo.getDatabase(dbName);

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

		MongoCollection<Document> table = database.getCollection(USERS_TABLE);
		
		Document document = new Document();
		document.put("username", username);
		document.put("password", PasswordHashService.createHash(password));
		document.put("createdDate", new Date());
		
		table.insertOne(document);
	}

	public static boolean checkUserPassword(String username, String password) {
		
		MongoCollection<Document> table = database.getCollection(USERS_TABLE);
		FindIterable<Document> users = table.find(eq("username", username));
		
		for (Document user : users) {
			String pValue = user.getString("password");
			if (PasswordHashService.validatePassword(password, pValue)) {
				return true;
			}
		}
		
		return false;

	}

	public static void addUser(String username, String password) {
		String hash = PasswordHashService.createHash(password);
		if (hash != null) {
			MongoCollection<Document> table = database.getCollection(USERS_TABLE);
			
			Document document = new Document();
			document.put("username", username);
			document.put("password", hash);
			document.put("createdDate", new Date());
			
			table.insertOne(document);
		}
	}

	public static void removeUser(String username) {
		MongoCollection<Document> table = database.getCollection(USERS_TABLE);
		table.deleteOne(eq("username", username));
	}

	public static List<String> listUsernames() {
		MongoCollection<Document> table = database.getCollection(USERS_TABLE);
		
		List<String> result = new ArrayList<String>();
		
		for (Document cur : table.find()) {
			result.add(cur.getString("username"));
		}
		
		return result;
	}

	public static void addFile(String fileName, String comment, String creator) {
		MongoCollection<Document> table = database.getCollection(FILES_TABLE);
		
		Document document = new Document();
		document.put("fileName", fileName);
		document.put("comment", comment);
		document.put("creator", creator);
		document.put("createdDate", new Date());
		
		table.insertOne(document);
	}

	public static void deleteFile(String fileName) {
		MongoCollection<Document> table = database.getCollection(FILES_TABLE);
		table.deleteOne(eq("fileName", fileName));
	}

	public static String getFileParameter(String fileName, String parameter) {
		MongoCollection<Document> table = database.getCollection(FILES_TABLE);
		Document myDoc = table.find(eq("fileName", fileName)).first();
		if(myDoc == null){
			return "";
		}
		return myDoc.getString(parameter);
	}

}
