package miretz.ycloud.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigurationService {

	private final static Logger logger = Logger.getLogger(ConfigurationService.class.getName());

	private static final Properties properties;

	static {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream stream = classLoader.getResourceAsStream("config.properties");
		properties = new Properties();

		if (stream != null) {
			try {
				properties.load(stream);
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Failed to load properties!", e);
				throw new RuntimeException(e.getMessage(), e);
			}
		}
	}

	public static String getProperty(String property) {
		String propertyValue = properties.getProperty(property);

		// openshift specific values
		if (propertyValue.equals("OPENSHIFT")) {
			propertyValue = System.getenv("OPENSHIFT_DATA_DIR");
		}
		// mongodb://$OPENSHIFT_MONGODB_DB_HOST:$OPENSHIFT_MONGODB_DB_PORT/
		if (propertyValue.equals("OPENSHIFT_MONGO")) {
			propertyValue = System.getenv("OPENSHIFT_MONGODB_DB_HOST") + ":" + System.getenv("OPENSHIFT_MONGODB_DB_PORT");
		}

		return propertyValue;
	}

	public static boolean getBooleanProperty(String property) {
		String propertyValue = properties.getProperty(property);
		return Boolean.parseBoolean(propertyValue);
	}

}
