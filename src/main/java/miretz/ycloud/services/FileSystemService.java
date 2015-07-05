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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.inject.Singleton;

import miretz.ycloud.models.Document;
import miretz.ycloud.services.utils.CustomNameFileResource;
import miretz.ycloud.services.utils.DirectoryFilenameFilter;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.vaadin.server.FileResource;

@Singleton
public class FileSystemService implements DocumentService {

	private final static Logger logger = Logger.getLogger(FileSystemService.class.getName());

	protected static final String[] IMAGE_FORMATS = { "jpg", "png", "bmp", "gif" };
	protected static final int BUFFER = 2048;

	protected final String dateFormat;
	protected final Dimension thumbnailDimensions;

	protected final File thumbnailDir;
	protected final File uploadDir;

	@Inject
	public FileSystemService(@Named("thumbnailDir") String thumbnailDir, @Named("uploadDir") String uploadDir, @Named("dateFormat") String dateFormat) {

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
	public boolean deleteFile(final Document document) {

		String contentId = document.getContentId();

		// delete thumbnail
		File thumbnail = getFileFromDir(thumbnailDir, contentId);
		if (thumbnail != null) {
			thumbnail.delete();
		}

		// delete file
		File file = getFileFromDir(uploadDir, contentId);
		if (file != null) {
			return file.delete();
		}

		return false;

	}

	@Override
	public void deleteAllFiles() {

		if (logger.isDebugEnabled()) {
			logger.debug("Delete all files started!");
		}

		for (File file : uploadDir.listFiles()) {
			file.delete();
		}
		for (File file : thumbnailDir.listFiles()) {
			file.delete();
		}

	}

	@Override
	public String getModifiedDate(Document document) {
		File file = getFileFromDir(uploadDir, document.getContentId());
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		long timestamp = file.lastModified();
		return sdf.format(timestamp);
	}

	@Override
	public FileResource getFileResource(Document document) {
		return new CustomNameFileResource(getFileFromDir(uploadDir, document.getContentId()), document.getFileName());
	}

	@Override
	public FileResource getThumbnailFileResource(Document document) {
		File thumbnail = getFileFromDir(thumbnailDir, document.getContentId());
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
		for (File file : uploadDir.listFiles()) {
			fileSizes += file.length();
		}
		return getSizeInMbDouble(fileSizes);
	}

	@Override
	public double getSizeInMbDouble(long size) {
		double sizeMb = size / 1024.0 / 1024.0;
		return Math.round(sizeMb * 100.0) / 100.0;
	}

	@Override
	public void saveThumbnail(Document document) throws IOException {

		File imgFile = getFileFromDir(uploadDir, document.getContentId());
		String imgExtension = getFileMimeType(document);
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
			ImageIO.write(cropped, imgExtension, new File(thumbnailDir.getAbsolutePath() + File.separator + document.getContentId()));
		}

	}

	@Override
	public InputStream getAllFilesZip(List<Document> documents) {
		try {
			final File f = new File(uploadDir.getAbsolutePath() + File.separator + "all_files.zip");
			if (f.exists() && f.isFile()) {
				f.delete();
			}

			List<File> files = new ArrayList<File>();
			for (Document doc : documents) {
				files.add(getFileFromDir(uploadDir, doc.getContentId()));
			}

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
			logger.error("Zip file not found!", e);
			return null;
		} catch (IOException e1) {
			logger.error("Failed to create Zip file!", e1);
			return null;
		}
	}

	private File getFileFromDir(File dir, String contentId) {
		File file = null;
		File[] files = dir.listFiles(new DirectoryFilenameFilter(contentId));
		if (files.length == 1) {
			file = files[0];
		}
		return file;
	}

	@Override
	public File getFile(Document document) {
		return getFileFromDir(uploadDir, document.getContentId());
	}

	@Override
	public String getFileMimeType(Document document) {
		String name = document.getFileName();
		int lastIndexOf = name.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return ""; // empty extension
		}
		return name.substring(lastIndexOf + 1);
	}

}
