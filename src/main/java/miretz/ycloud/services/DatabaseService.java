package miretz.ycloud.services;

import java.util.List;

import miretz.ycloud.models.Document;

public interface DatabaseService {

	/** user interaction */

	boolean checkUserPassword(String username, String password);

	void addUser(String username, String password);

	void removeUser(String username);

	List<String> listUsernames();

	/** file interaction */

	void addDocument(Document document);

	List<Document> getAllDocuments();

	List<Document> getDescendants(String parentId);

	Document getDocument(String contentId);

	Document findByMetadataValue(String key, String value);

	void deleteDocument(String contentId);

	void deleteAllDocuments();

}
