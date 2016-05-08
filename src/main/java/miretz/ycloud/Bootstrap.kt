package miretz.ycloud

import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.name.Names
import com.google.inject.servlet.GuiceServletContextListener
import com.google.inject.servlet.ServletModule
import miretz.ycloud.services.DatabaseService
import miretz.ycloud.services.DocumentService
import miretz.ycloud.services.FileSystemService
import miretz.ycloud.services.MongoDBService
import java.io.IOException
import java.util.*
import javax.servlet.annotation.WebListener

@WebListener class Bootstrap : GuiceServletContextListener() {
    override fun getInjector(): Injector {
        return Guice.createInjector(object : ServletModule() {
            override fun configureServlets() {
                serve("/*").with(GuiceApplicationServlet::class.java)

                val classLoader = Thread.currentThread().contextClassLoader
                val stream = classLoader.getResourceAsStream("config.properties")
                val properties = Properties()

                if (stream != null) {
                    try {
                        properties.load(stream)

                        Names.bindProperties(binder(), properties)

                    } catch (e: IOException) {
                        throw RuntimeException(e.message, e)
                    }

                }

                bind<DatabaseService>(DatabaseService::class.java).to(MongoDBService::class.java)
                bind<DocumentService>(DocumentService::class.java).to(FileSystemService::class.java)

            }
        })
    }
}