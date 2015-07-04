package miretz.ycloud.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.vaadin.server.FileResource;

public interface DocumentService {

	boolean deleteFile(String fileName);

	void deleteAllFiles();

	String getModifiedDate(String fileName);

	FileResource getFileResource(String fileName);

	FileResource getThumbnailFileResource(String fileName);

	double getFreeSpace();

	double getSizeOfFiles();

	double getSizeInMbDouble(long size);

	void saveThumbnail(String fileName) throws IOException;

	InputStream getAllFilesZip(List<String> filenames);

	File getFile(String fileName);

	String getFileMimeType(String fileName);

}