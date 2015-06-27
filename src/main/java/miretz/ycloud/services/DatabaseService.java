package miretz.ycloud.services;

import java.util.List;

public interface DatabaseService {
	
	boolean checkUserPassword(String username, String password);

	void addUser(String username, String password);
	
	void removeUser(String username);
	
	List<String> listUsernames();
	
	void addFile(String fileName, String comment, String creator);

	void deleteFile(String fileName);
	
	String getFileComment(String fileName);
	
	String getFileCreator(String fileName);
	
}
