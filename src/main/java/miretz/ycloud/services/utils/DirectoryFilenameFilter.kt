package miretz.ycloud.services.utils

import java.io.File
import java.io.FilenameFilter

public class DirectoryFilenameFilter(protected val filename: String) : FilenameFilter {

    override fun accept(dir: File, name: String): Boolean {
        return filename == name
    }

}
