package miretz.ycloud.services;

import java.util.List;

import miretz.ycloud.dbconnectors.MongoDbConnector;

public class DatabaseService {
	
	public static boolean checkUserPassword(String username, String password){
		return MongoDbConnector.checkUserPassword(username, password);
	}

	public static void addUser(String username, String password) {
		MongoDbConnector.addUser(username, password);		
	}
	
	public static void removeUser(String username) {
		MongoDbConnector.removeUser(username);
	}
	
	public static List<String> listUsernames(){
		return MongoDbConnector.listUsernames();
	}
	
	public static void addFile(String fileName, String comment, String creator) {
		MongoDbConnector.addFile(fileName, comment, creator);
	}

	public static void deleteFile(String fileName){
		MongoDbConnector.deleteFile(fileName);
	}
	
	public static String getFileComment(String fileName){
		return MongoDbConnector.getFileParameter(fileName,"comment");
	}
	
	public static String getFileCreator(String fileName){
		return MongoDbConnector.getFileParameter(fileName,"creator");
	}
	

	
}
