package c8y.trackeragent.utils;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import c8y.trackeragent.protocol.coban.CobanConstants;

import com.cumulocity.sdk.client.SDKException;

public class ConfigUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigUtils.class);
    
    public static final String CONFIG_FILE_NAME = "tracker-agent-server.properties";
    public static final String LOG_CONFIG_FILE_NAME = "tracker-agent-logback.xml";
    public static final String DEVICES_FILE_NAME = "device.properties";
    
    private static final String PLATFORM_HOST_PROP = "C8Y.baseURL";    
    private static final String FORCE_INITIAL_HOST_PROP = "C8Y.forceInitialHost";    
    private static final boolean DEFAULT_FORCE_INITIAL_HOST = false;
    private static final String LOCAL_SOCKET_PORT_PROP = "localPort";
    private static final String DEFAULT_LOCAL_SOCKET_PORT = "9090";
    private static final String BOOTSTRAP_USER_PROP = "C8Y.devicebootstrap.user";
    private static final String BOOTSTRAP_PASSWORD_PROP = "C8Y.devicebootstrap.password";
    private static final String CLIENT_TIMEOUT_PROP = "client.timeout";
    private static final String COBAN_LOCATION_REPORT_INTERVAL_PROP = "coban.locationReport.timeInterval";
    private static final String DEFAULT_CLIENT_TIMEOUT = "" + TimeUnit.MINUTES.toMillis(5);
    private static final Random random = new Random();
    
    private static final ConfigUtils instance = new ConfigUtils();
    
    /**
     * Path to the folder with configuration files: common.properties and device.properties
     * On production it's /etc/tracker-agent.
     * On tests it's target/test-classes.
     */
    public static ConfigUtils get() {
        return instance;
    }


    public String getConfigFilePath(String fileName) {
        return "/etc/tracker-agent/" + fileName;
    }
    
    public static Properties getProperties(String path) throws SDKException {
        Properties source = new Properties();
        InputStream io = null;
        try {
            io = new FileInputStream(path);
            source.load(io);
            return source;
        } catch (IOException ioex) {
            throw new SDKException("Can't load configuration from file system " + path, ioex);
        } finally {
            IOUtils.closeQuietly(io);
        }
    }
    
    public TrackerConfiguration loadCommonConfiguration() {
        String sourceFilePath = getConfigFilePath(CONFIG_FILE_NAME);
        Properties props = getProperties(sourceFilePath);
        int clientTimeout = parseInt(getProperty(props, CLIENT_TIMEOUT_PROP, DEFAULT_CLIENT_TIMEOUT));
        //@formatter:off
        TrackerConfiguration config = new TrackerConfiguration()
            .setPlatformHost(getProperty(props, PLATFORM_HOST_PROP))
            .setForceInitialHost(getBooleanProperty(props, FORCE_INITIAL_HOST_PROP, DEFAULT_FORCE_INITIAL_HOST))
            .setLocalPort(getSocketPort(props))
            .setBootstrapUser(getProperty(props, BOOTSTRAP_USER_PROP))
            .setBootstrapPassword(getProperty(props, BOOTSTRAP_PASSWORD_PROP))
            .setBootstrapTenant("management")
            .setCobanLocationReportTimeInterval(props.getProperty(COBAN_LOCATION_REPORT_INTERVAL_PROP, CobanConstants.DEFAULT_LOCATION_REPORT_INTERVAL))
            .setClientTimeout(clientTimeout);
        //@formatter:on
        logger.info(format("Configuration loaded from: %s: %s", sourceFilePath, config));
        return config;

    }
    
    private String getProperty(Properties props, String key) {
        String value = getProperty(props, key, null);
        if (value == null) {
            throw new RuntimeException("Missing property \'" + key + "\' in file " + CONFIG_FILE_NAME);
        }
        return value;
    }
    
    private String getProperty(Properties props, String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }
    
    private boolean getBooleanProperty(Properties props, String key, Boolean defaultValue) {
        return Boolean.parseBoolean(getProperty(props, key, defaultValue.toString()));
    }
    
    private int getSocketPort(Properties props) {
        String port = getProperty(props, LOCAL_SOCKET_PORT_PROP, DEFAULT_LOCAL_SOCKET_PORT);
        return "$random".equals(port) ? randomPort() : parseInt(port);
    }
    
    private static int randomPort() {
        return random.nextInt(20000) + 40000;
    }

}
