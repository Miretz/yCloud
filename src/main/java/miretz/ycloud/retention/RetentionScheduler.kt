package miretz.ycloud.retention

import com.google.inject.Inject
import com.vaadin.server.Page
import miretz.ycloud.services.DatabaseService
import miretz.ycloud.services.DocumentService
import org.apache.log4j.Logger
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Singleton
class RetentionScheduler
@Inject
constructor(protected val documentService: DocumentService, protected val databaseService: DatabaseService) {

    protected val scheduler: ScheduledExecutorService

    init {
        //check all file retention and delete
        scheduler = Executors.newScheduledThreadPool(1)
        scheduler.scheduleAtFixedRate({
            checkRetention()
        }, 20, 1, TimeUnit.SECONDS)
    }

    fun checkRetention() {
        val allDocs = databaseService.getAllDocuments();
        allDocs.forEach {
            if (it.retentionDate != null && it.retentionDate.before(Date())) {
                databaseService.deleteDocument(it.contentId)
                documentService.deleteFile(it)
            }
        }
    }
}
