package miretz.ycloud.services;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.inject.Singleton;

import miretz.ycloud.models.Document;
import miretz.ycloud.services.utils.DirectoryFilenameFilter;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.vaadin.server.FileResource;

@Singleton
public class FileSystemService implements DocumentService {

	private final static Logger logger = Logger.getLogger(FileSystemService.class.getName());

	protected static final String[] IMAGE_FORMATS = { "jpg", "png", "bmp", "gif" };
	protected static final int BUFFER = 2048;

	protected final DatabaseService databaseService;
	protected final String dateFormat;
	protected final Dimension thumbnailDimensions;

	protected final File thumbnailDir;
	protected final File uploadDir;

	@Inject
	public FileSystemService(@Named("thumbnailDir") String thumbnailDir, @Named("uploadDir") String uploadDir, @Named("dateFormat") String dateFormat, DatabaseService databaseService) {
		this.databaseService = databaseService;
		this.dateFormat = dateFormat;
		this.thumbnailDimensions = new Dimension(50, 50);

		this.uploadDir = new File(uploadDir);
		if (this.uploadDir == null || !this.uploadDir.exists() || !this.uploadDir.isDirectory()) {
			throw new RuntimeException("Upload directory not found" + uploadDir);
		}

		this.thumbnailDir = new File(thumbnailDir);
		if (this.thumbnailDir == null || !this.thumbnailDir.exists() || !this.thumbnailDir.isDirectory()) {
			throw new RuntimeException("Thumbnail directory not found" + uploadDir);
		}

	}

	@Override
	public List<Document> getAllFilesAsDocuments() {
		List<Document> results = new ArrayList<Document>();

		File[] files = uploadDir.listFiles();
		for (File file : files) {
			if (file.isFile()) {
				String fileName = file.getName();
				String mime = getFileExtension(file).toLowerCase();
				String comment = databaseService.getFileComment(fileName);
				String creator = databaseService.getFileCreator(fileName);
				if (creator == null || creator.isEmpty()) {
					creator = "Admin";
				}
				results.add(new Document(fileName, file.length(), mime, comment, creator));
			}
		}

		logger.log(Level.INFO, "Files found: " + results.size());

		return results;
	}

	@Override
	public boolean deleteFile(final String filename) {

		databaseService.deleteFile(filename);

		// delete thumbnail
		File thumbnail = getFileFromDir(thumbnailDir, filename);
		if (thumbnail != null) {
			thumbnail.delete();
		}

		// delete file
		File file = getFileFromDir(uploadDir, filename);
		if (file != null) {
			return file.delete();
		}

		return false;

	}

	@Override
	public void deleteAllFiles() {

		logger.entering(getClass().getName(), "Delete all files started!");

		for (File file : uploadDir.listFiles()) {
			databaseService.deleteFile(file.getName());
			file.delete();
		}
		for (File file : thumbnailDir.listFiles()) {
			file.delete();
		}
	}

	@Override
	public String getModifiedDate(String filename) {
		File file = getFileFromDir(uploadDir, filename);
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		long timestamp = file.lastModified();
		return sdf.format(timestamp);
	}

	@Override
	public FileResource getFileResource(String filename) {
		return new FileResource(getFileFromDir(uploadDir, filename));
	}

	@Override
	public FileResource getThumbnailFileResource(String filename) {
		File thumbnail = getFileFromDir(thumbnailDir, filename);
		if (thumbnail != null && thumbnail.exists() && thumbnail.isFile()) {
			return new FileResource(thumbnail);
		} else {
			return null;
		}
	}

	@Override
	public double getFreeSpace() {
		long freeSpace = uploadDir.getFreeSpace();
		return getSizeInMbDouble(freeSpace);
	}

	@Override
	public double getSizeOfFiles() {
		long fileSizes = 0L;
		for (Document document : getAllFilesAsDocuments()) {
			fileSizes += document.getSize();
		}
		return getSizeInMbDouble(fileSizes);
	}

	@Override
	public double getSizeInMbDouble(long size) {
		double sizeMb = size / 1024.0 / 1024.0;
		return Math.round(sizeMb * 100.0) / 100.0;
	}

	private String getFileExtension(File file) {
		String name = file.getName();
		int lastIndexOf = name.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return ""; // empty extension
		}
		return name.substring(lastIndexOf + 1);
	}

	@Override
	public void saveThumbnail(String fileName) throws IOException {

		File imgFile = getFileFromDir(uploadDir, fileName);
		String imgExtension = getFileExtension(imgFile);
		if (Arrays.asList(IMAGE_FORMATS).contains(imgExtension)) {

			BufferedImage sourceImage = ImageIO.read(imgFile);

			int xPos = 0;
			int yPos = 0;

			Image scaled = null;
			BufferedImage img = null;

			if (sourceImage.getWidth() > sourceImage.getHeight()) {
				scaled = sourceImage.getScaledInstance(-1, thumbnailDimensions.height, Image.SCALE_SMOOTH);
				img = new BufferedImage(scaled.getWidth(null), thumbnailDimensions.height, BufferedImage.TYPE_INT_RGB);
				xPos = (int) ((img.getWidth() / 2) - (thumbnailDimensions.getWidth() / 2));
			} else {
				scaled = sourceImage.getScaledInstance(thumbnailDimensions.width, -1, Image.SCALE_SMOOTH);
				img = new BufferedImage(thumbnailDimensions.width, scaled.getHeight(null), BufferedImage.TYPE_INT_RGB);
				yPos = (int) ((img.getHeight() / 2) - (thumbnailDimensions.getHeight() / 2));
			}

			img.createGraphics().drawImage(scaled, 0, 0, null);
			BufferedImage cropped = img.getSubimage(xPos, yPos, thumbnailDimensions.width, thumbnailDimensions.height);
			ImageIO.write(cropped, imgExtension, new File(thumbnailDir.getAbsolutePath() + File.separator + fileName));
		}

	}

	@Override
	public InputStream getAllFilesZip() {
		try {
			final File f = new File(uploadDir.getAbsolutePath() + File.separator + "all_files.zip");
			if (f.exists() && f.isFile()) {
				f.delete();
			}

			File[] files = uploadDir.listFiles();
			final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
			for (File file : files) {
				if (file.isFile()) {
					ZipEntry e = new ZipEntry(file.getName());
					out.putNextEntry(e);
					byte data[] = new byte[BUFFER];
					FileInputStream fileInputStream = new FileInputStream(file);
					BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream, BUFFER);
					int size = -1;
					while ((size = bufferedInputStream.read(data, 0, BUFFER)) != -1) {
						out.write(data, 0, size);
					}
					bufferedInputStream.close();
					out.closeEntry();
				}
			}
			out.close();

			return new FileInputStream(f);
		} catch (FileNotFoundException e) {
			logger.log(Level.SEVERE, "Zip file not found!", e);
			return null;
		} catch (IOException e1) {
			logger.log(Level.SEVERE, "Failed to create Zip file!", e1);
			return null;
		}
	}

	private File getFileFromDir(File dir, String filename) {
		File file = null;
		File[] files = dir.listFiles(new DirectoryFilenameFilter(filename));
		if (files.length == 1) {
			file = files[0];
		}
		return file;
	}

}
