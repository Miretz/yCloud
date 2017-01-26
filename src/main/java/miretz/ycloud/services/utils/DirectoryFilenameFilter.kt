package miretz.ycloud.services.utils

import java.io.File
import java.io.FilenameFilter

class DirectoryFilenameFilter(val filename: String) : FilenameFilter {

    override fun accept(dir: File, name: String): Boolean {
        return filename == name
    }

}
