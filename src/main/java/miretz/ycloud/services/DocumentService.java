package miretz.ycloud.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import miretz.ycloud.models.Document;

import com.vaadin.server.FileResource;

public interface DocumentService {

	boolean deleteFile(Document document);

	void deleteAllFiles();

	String getModifiedDate(Document document);

	FileResource getFileResource(Document document);

	FileResource getThumbnailFileResource(Document document);

	double getFreeSpace();

	double getSizeOfFiles();

	double getSizeInMbDouble(long size);

	void saveThumbnail(Document document) throws IOException;

	InputStream getAllFilesZip(List<Document> documents);

	File getFile(Document document);

	String getFileMimeType(Document document);

}