package miretz.ycloud.services.utils;

import java.io.File;
import java.io.FilenameFilter;

public class DirectoryFilenameFilter implements FilenameFilter {

	protected final String filename;

	public DirectoryFilenameFilter(final String filename) {
		this.filename = filename;
	}

	@Override
	public boolean accept(File dir, String name) {
		return filename.equals(name);
	}

}
