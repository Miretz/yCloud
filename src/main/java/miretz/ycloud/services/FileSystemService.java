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

import com.google.inject.name.Named;

import miretz.ycloud.models.Document;

import com.google.inject.Inject;
import com.vaadin.server.FileResource;

@Singleton
public class FileSystemService implements DocumentService {

	private final static Logger logger = Logger.getLogger(FileSystemService.class.getName());

	private static final String DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";
	private static final int BUFFER = 2048;
	public static final String[] IMAGE_FORMATS = { "jpg", "png", "bmp", "gif" };
	public static final Dimension THUMB_DIMENSION = new Dimension(50, 50);

	protected DatabaseService databaseService;
	protected String thumbnailDir;
	protected String uploadDir;

	@Inject
	public FileSystemService(@Named("thumbnailDir") String thumbnailDir, @Named("uploadDir") String uploadDir, DatabaseService databaseService) {
		this.thumbnailDir = thumbnailDir;
		this.uploadDir = uploadDir;
		this.databaseService = databaseService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see miretz.ycloud.services.DocumentService#getAllFilesAsDocuments()
	 */
	@Override
	public List<Document> getAllFilesAsDocuments() {
		List<Document> results = new ArrayList<Document>();
		File rootDir = new File(uploadDir);
		if (rootDir == null || !rootDir.exists()) {
			throw new RuntimeException("Upload directory not found" + uploadDir);
		}

		File[] files = rootDir.listFiles();
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
		return results;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see miretz.ycloud.services.DocumentService#deleteFile(java.lang.String)
	 */
	@Override
	public boolean deleteFile(String fileName) {
		databaseService.deleteFile(fileName);
		File fileThumb = new File(thumbnailDir + fileName);
		if (fileThumb.exists()) {
			fileThumb.delete();
		}
		File file = new File(uploadDir + fileName);
		return file.delete();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see miretz.ycloud.services.DocumentService#deleteAllFiles()
	 */
	@Override
	public void deleteAllFiles() {
		File uploadDirectory = new File(uploadDir);
		if (uploadDirectory.exists() && uploadDirectory.isDirectory()) {
			for (File file : uploadDirectory.listFiles()) {
				databaseService.deleteFile(file.getName());
				file.delete();
			}
		}
		File thumbnailDirectory = new File(thumbnailDir);
		if (thumbnailDirectory.exists() && thumbnailDirectory.isDirectory()) {
			for (File file : thumbnailDirectory.listFiles())
				file.delete();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * miretz.ycloud.services.DocumentService#getModifiedDate(java.lang.String)
	 */
	@Override
	public String getModifiedDate(String fileName) {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		long timestamp = new File(uploadDir + fileName).lastModified();
		return sdf.format(timestamp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * miretz.ycloud.services.DocumentService#getFileResource(java.lang.String)
	 */
	@Override
	public FileResource getFileResource(String fileName) {
		return new FileResource(new File(uploadDir + fileName));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * miretz.ycloud.services.DocumentService#getFileInputStream(java.lang.String
	 * )
	 */
	@Override
	public InputStream getFileInputStream(String fileName) throws FileNotFoundException {
		return new FileInputStream(uploadDir + fileName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * miretz.ycloud.services.DocumentService#getThumbnailFileResource(java.
	 * lang.String)
	 */
	@Override
	public FileResource getThumbnailFileResource(String fileName) {
		File thumbnail = new File(thumbnailDir + fileName);
		if (thumbnail.exists() && thumbnail.isFile()) {
			return new FileResource(thumbnail);
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see miretz.ycloud.services.DocumentService#getFreeSpace()
	 */
	@Override
	public double getFreeSpace() {
		File file = new File(uploadDir);
		long freeSpace = file.getFreeSpace();
		return getSizeInMbDouble(freeSpace);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see miretz.ycloud.services.DocumentService#getSizeOfFiles()
	 */
	@Override
	public double getSizeOfFiles() {
		long fileSizes = 0L;
		for (Document document : getAllFilesAsDocuments()) {
			fileSizes += document.getSize();
		}
		return getSizeInMbDouble(fileSizes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see miretz.ycloud.services.DocumentService#getSizeInMbDouble(long)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * miretz.ycloud.services.DocumentService#saveThumbnail(java.lang.String)
	 */
	@Override
	public void saveThumbnail(String fileName) throws IOException {
		checkThumbnailDir();
		File imgFile = new File(uploadDir + fileName);
		String imgExtension = getFileExtension(imgFile);
		if (Arrays.asList(IMAGE_FORMATS).contains(imgExtension)) {

			BufferedImage sourceImage = ImageIO.read(imgFile);

			int xPos = 0;
			int yPos = 0;

			Image scaled = null;
			BufferedImage img = null;

			if (sourceImage.getWidth() > sourceImage.getHeight()) {
				scaled = sourceImage.getScaledInstance(-1, THUMB_DIMENSION.height, Image.SCALE_SMOOTH);
				img = new BufferedImage(scaled.getWidth(null), THUMB_DIMENSION.height, BufferedImage.TYPE_INT_RGB);
				xPos = (int) ((img.getWidth() / 2) - (THUMB_DIMENSION.getWidth() / 2));
			} else {
				scaled = sourceImage.getScaledInstance(THUMB_DIMENSION.width, -1, Image.SCALE_SMOOTH);
				img = new BufferedImage(THUMB_DIMENSION.width, scaled.getHeight(null), BufferedImage.TYPE_INT_RGB);
				yPos = (int) ((img.getHeight() / 2) - (THUMB_DIMENSION.getHeight() / 2));
			}

			img.createGraphics().drawImage(scaled, 0, 0, null);
			BufferedImage cropped = img.getSubimage(xPos, yPos, THUMB_DIMENSION.width, THUMB_DIMENSION.height);
			ImageIO.write(cropped, imgExtension, new File(thumbnailDir + fileName));
		}

	}

	private void checkThumbnailDir() {
		File temp = new File(thumbnailDir);
		if (!temp.exists()) {
			temp.mkdir();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see miretz.ycloud.services.DocumentService#getAllFilesZip()
	 */
	@Override
	public InputStream getAllFilesZip() {
		try {
			final File f = new File(uploadDir + "all_files.zip");
			if (f.exists() && f.isFile()) {
				f.delete();
			}
			File[] files = new File(uploadDir).listFiles();
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
			return new FileInputStream(uploadDir + f.getName());
		} catch (FileNotFoundException e) {
			logger.log(Level.SEVERE, "Zip file not found!", e);
			return null;
		} catch (IOException e1) {
			logger.log(Level.SEVERE, "Failed to create Zip file!", e1);
			return null;
		}
	}

}
