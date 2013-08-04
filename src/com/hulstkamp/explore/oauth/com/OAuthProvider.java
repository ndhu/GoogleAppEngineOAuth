package com.hulstkamp.explore.oauth.com;

import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: hulstkan
 * Date: 30.07.13
 * Time: 22:03
 * To change this template use File | Settings | File Templates.
 */
public class OAuthProvider implements IOAuthProvider {


    private static enum PropertyKeyName {

        AUTHORIZE_URI("oauth.authorize.uri"),
        AUTHORIZE_PARAMS("oauth.authorize.params"),
        REDIRECT("oauth.redirect"),
        ACCESS_TOKEN_URI("oauth.access.token.uri"),
        ACCESS_TOKEN_PARAMS("oauth.access.token.params"),
        ACCESS_TOKEN_METHOD("oauth.access.token.method"),
        ACCESS_TOKEN_RESPONSE_CLASS("oauth.access.token.response.class", Type.CLAZZ),
        USER_API_URI("oauth.user.api.uri"),
        CLIENT_SECRET("client.secret"),
        CLIENT_ID("client.id"),
        SERVER("server");

        private enum Type {
            STRING,
            CLAZZ;
        }
        private String name;

        private Type type;
        PropertyKeyName(String name) {
            this(name, Type.STRING);
        }

        PropertyKeyName(String name, Type type) {
            this.name = name;
            this.type = type;
        }

        String getName() {
            return name;
        }

        Type getType() {
            return type;
        }

    }

    public static enum OauthPhase {
        AUTHORIZATION,
        INVALID,
        AUTHORIZATION_RESPONSE
    }

    //immutables by code
    private String name;
    private String server;
    private String authorizationUrl;
    private String params;
    private String redirectUrl;
    private String clientId;
    private String oauthTokenLocation;
    private String oauthTokenLocationParams;
    private String accessTokenRequestType;
    private Class accessTokenResponseClass;
    private String userProfileUrl;
    private String clientSecret;
    private OAuthUser.OauthOrigin origin;
    private OauthPhase phase;

    private OAuthProvider() {
        super();
    }
    public static OAuthProvider create (String providerName, Properties properties) {
        OAuthProvider provider = new OAuthProvider();
        provider.name = providerName;
        provider.origin = OAuthUser.OauthOrigin.getOriginByName(providerName);
        provider.authorizationUrl = (String) getProperty(providerName, properties, PropertyKeyName.AUTHORIZE_URI);
        provider.params = (String) getProperty(providerName, properties, PropertyKeyName.AUTHORIZE_PARAMS);;
        provider.redirectUrl = (String) getProperty(providerName, properties, PropertyKeyName.REDIRECT);
        provider.clientId = (String) getProperty(providerName, properties, PropertyKeyName.CLIENT_ID);
        provider.oauthTokenLocation = (String) getProperty(providerName, properties, PropertyKeyName.ACCESS_TOKEN_URI);
        provider.oauthTokenLocationParams = (String) getProperty(providerName, properties, PropertyKeyName.ACCESS_TOKEN_PARAMS);
        provider.accessTokenResponseClass = (Class) getProperty(providerName, properties, PropertyKeyName.ACCESS_TOKEN_RESPONSE_CLASS);
        provider.accessTokenRequestType = (String) getProperty(providerName, properties, PropertyKeyName.ACCESS_TOKEN_METHOD);
        provider.userProfileUrl = (String) getProperty(providerName, properties, PropertyKeyName.USER_API_URI);
        provider.clientSecret = (String) getProperty(providerName, properties, PropertyKeyName.CLIENT_SECRET);
        provider.server = (String) getProperty(providerName, properties, PropertyKeyName.SERVER);
        return provider;
    }

    private static Object getProperty(String providerName, Properties properties, PropertyKeyName propertyName) {
        return getProperty(providerName, properties, propertyName, "");
    }

    private static Object getProperty(String providerName, Properties properties, PropertyKeyName propertyName, String fallback) {
        Object propertyValue = properties.get(providerName + "." + propertyName.getName());
        if(propertyName.getType().equals(PropertyKeyName.Type.CLAZZ)) {
            try {
                propertyValue = Class.forName((String) propertyValue);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return (propertyValue != null ? propertyValue : fallback);
    }

    @Override
    public String getAuthorizationRequestUrl() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.authorizationUrl);
        sb.append("?");
        sb.append(this.params);
        return sb.toString();
    }

    @Override
    public String getName() {
        return name;
    }

    public String getAuthorizationUrl() {
        return authorizationUrl;
    }

    @Override
    public String getParams() {
        return this.params;
    }

    @Override
    public String getRedirectUrl() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.server);
        sb.append(this.redirectUrl);
        return sb.toString();
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public String getOauthTokenLocation() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.oauthTokenLocation);
        sb.append("?");
        sb.append(this.oauthTokenLocationParams);
        return sb.toString();
    }

    @Override
    public String createUserProfileUrl(String accessToken) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.userProfileUrl);
        sb.append("?access_token=");
        sb.append(accessToken);
        return sb.toString();
    }

    @Override
    public String getClientSecret() {
        return this.clientSecret;
    }

    @Override
    public OAuthUser.OauthOrigin getOrigin() {
        return this.origin;
    }

    @Override
    public Class getAccessTokenResponseClass() {
        return accessTokenResponseClass;
    }

    @Override
    public String getAccessTokenRequestType() {
        return accessTokenRequestType;
    }

    public OauthPhase getPhase() {
        return phase;
    }

    public void setPhase(OauthPhase phase) {
        this.phase = phase;
    }

}
