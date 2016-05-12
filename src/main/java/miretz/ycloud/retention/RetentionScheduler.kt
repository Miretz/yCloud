package miretz.ycloud.retention

import com.google.inject.Inject
import com.google.inject.name.Named
import miretz.ycloud.services.DatabaseService
import miretz.ycloud.services.DocumentService
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Singleton
class RetentionScheduler
@Inject
constructor(protected val documentService: DocumentService, protected val databaseService: DatabaseService, @Named("retentionCheckInterval") retentionCheckInterval : Long) {

    protected val scheduler: ScheduledExecutorService

    init {
        //check all file retention and delete
        scheduler = Executors.newScheduledThreadPool(1)
        scheduler.scheduleAtFixedRate({
            checkRetention()
        }, retentionCheckInterval, 1, TimeUnit.SECONDS)
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
