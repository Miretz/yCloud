package miretz.ycloud;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.annotation.WebListener;

import miretz.ycloud.services.DatabaseService;
import miretz.ycloud.services.DocumentService;
import miretz.ycloud.services.FileSystemService;
import miretz.ycloud.services.MongoDBService;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

@WebListener
public class Bootstrap extends GuiceServletContextListener {
    @Override
    protected Injector getInjector() {
        return Guice.createInjector(new ServletModule() {
            @Override
            protected void configureServlets() {
                serve("/*").with(GuiceApplicationServlet.class);
                
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        		InputStream stream = classLoader.getResourceAsStream("config.properties");
        		Properties properties = new Properties();

        		if (stream != null) {
        			try {
        				properties.load(stream);
        				
        				Names.bindProperties(binder(), properties);
        				
        			} catch (IOException e) {
        				throw new RuntimeException(e.getMessage(), e);
        			}
        		}
        		
        		bind(DatabaseService.class).to(MongoDBService.class);
        		bind(DocumentService.class).to(FileSystemService.class);
                
            }
        });
    }
}