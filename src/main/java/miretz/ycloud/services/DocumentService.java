package miretz.ycloud.services;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import miretz.ycloud.models.Document;

import com.vaadin.server.FileResource;

public interface DocumentService {

	List<Document> getAllFilesAsDocuments();

	boolean deleteFile(String fileName);

	void deleteAllFiles();

	String getModifiedDate(String fileName);

	FileResource getFileResource(String fileName);

	InputStream getFileInputStream(String fileName) throws FileNotFoundException;

	FileResource getThumbnailFileResource(String fileName);

	double getFreeSpace();

	double getSizeOfFiles();

	double getSizeInMbDouble(long size);

	void saveThumbnail(String fileName) throws IOException;

	InputStream getAllFilesZip();

}