package miretz.ycloud.services.utils;

import java.io.File;

import com.vaadin.server.FileResource;

@SuppressWarnings("serial")
public class CustomFileNameResource extends FileResource {

    protected final String filename;

    public CustomFileNameResource(File sourceFile, String filename) {
        super(sourceFile);
        this.filename = filename;
    }

    @Override
    public String getFilename() {
        return filename;
    }

}
