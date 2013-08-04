package com.hulstkamp.explore.oauth.com;

import com.google.appengine.api.utils.SystemProperty;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.appengine.api.utils.SystemProperty.Environment.Value.Development;
import static com.google.appengine.api.utils.SystemProperty.Environment.Value.Production;
import static com.google.appengine.api.utils.SystemProperty.environment;

/**
 * Created with IntelliJ IDEA.
 * User: hulstkan
 * Date: 30.07.13
 * Time: 20:19
 * To change this template use File | Settings | File Templates.
 */
public class OAuthProviderService implements ServletContextListener {

    private static final String SERVER;

    private static final String INVOKE_URL_REGEXP =  "(?i)^.*\\/oauth\\/(.*)/invoke\\/?";
    private static final String REDIRECT_URL_REGEXP =  "(?i)^.*\\/oauth\\/(.*)/redirect\\/?";
    private static final String EVAL_PROVIDER_REGEXP = "^.*\\/oauth\\/(.*)\\/(invoke|redirect)\\/?";

    static {
        SystemProperty.Environment.Value env = environment.value();
        if (env == Production) {
            SERVER = "http://localhost:8080";
        } else if (env == Development) {
            SERVER = "http://localhost:8080";
        } else {
            SERVER = "invalid";
        }
    }

    private static OAuthProviderService _instance;

    private ServletContext context = null;

    private HashMap<String, OAuthProvider> oauthProviders;

    //Singleton, refactor in a DI Environment
    public static OAuthProviderService getInstance() {
        return _instance;
    }

    //Web Application context is ready, initialize context
    public void contextInitialized(ServletContextEvent event) {

        //load, merge and inject all relevant properties
        Properties oauthConfigProps = loadProperties("WEB-INF/oauth.config");
        Properties secretsProps = loadProperties("WEB-INF/api-keys-secrets.config");
        Properties props = this.mergeProperties(new Properties[]{oauthConfigProps, secretsProps});
        props.put("facebook.server", SERVER);
        props.put("google.server", SERVER);

        //create providers from properties
        String providers = props.getProperty("providers");
        String[] providerNames = providers.toLowerCase().split(",");
        this.initOauthProviders(providerNames, props);

        _instance = this;
    }

    private Properties loadProperties(String filename) {
        Properties props = new Properties();
        try {
            FileReader fr = new FileReader(filename);
            props.load(fr);
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return props;
    }

    /**
     * Merges all properties provided in the sources
     * Leaves the sources intact and returns a new Properties-Object containing
     * all the properties of the sources.
     * If a property is defined in more than one source, the value defined in the source
     * that comes later in the array wins - it overrides the value that has been set before
     *
     * @param sources Array of Properties objects to use as sources
     * @return A new Properties object containing all the entries of the sources
     */
    private Properties mergeProperties(Properties[] sources) {
        Properties merged = new Properties();
        for (Properties source : sources) {
            merged.putAll(source);
        }
        return merged;
    }

    private void initOauthProviders(String[] providerNames, Properties properties) {
        HashMap<String, OAuthProvider> oauthProviders = new HashMap<String, OAuthProvider>(providerNames.length);
        for (String providerName : providerNames) {
            OAuthProvider oauthProvider = OAuthProvider.create(providerName, properties);
            oauthProviders.put(providerName, oauthProvider);
        }
        this.oauthProviders = oauthProviders;
    }

    //clean up when Web Application gets destroyed
    public void contextDestroyed(ServletContextEvent event) {
        this.context = null;
    }

    private OAuthProvider getOauthProvider(String name) {
        return this.oauthProviders.get(name);
    }

    public OAuthProvider getOauthProviderFor(String uri) {
        String invokeRegexp = "(?i)^.*\\/oauth\\/(facebook|google)/invoke\\/?";
        String redirectRegexp = "(?i)^.*\\/oauth\\/(facebook|google)/redirect\\/?";
        OAuthProvider oauthProvider = evalOauthProviderToUse(uri);
        if (oauthProvider != null) {
            if (uri.matches(invokeRegexp)) {
                oauthProvider.setPhase(OAuthProvider.OauthPhase.AUTHORIZATION);
            } else if (uri.matches(redirectRegexp)) {
                oauthProvider.setPhase(OAuthProvider.OauthPhase.AUTHORIZATION_RESPONSE);
            } else {
                oauthProvider.setPhase(OAuthProvider.OauthPhase.INVALID);
            }
            return oauthProvider;
        }
        return null;
    }

    private OAuthProvider evalOauthProviderToUse(String uri) {
        Pattern pattern = Pattern.compile(EVAL_PROVIDER_REGEXP, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(uri);
        matcher.find();
        int groupCount = matcher.groupCount();
        String oauthProviderMatch = "root";
        if (groupCount == 2) {
            oauthProviderMatch = matcher.group(1);
        }
        return OAuthProviderService.getInstance().getOauthProvider(oauthProviderMatch);
    }
}