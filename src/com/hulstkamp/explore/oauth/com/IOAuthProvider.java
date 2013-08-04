package com.hulstkamp.explore.oauth.com;

/**
 * Created with IntelliJ IDEA.
 * User: hulstkan
 * Date: 04.08.13
 * Time: 16:49
 * To change this template use File | Settings | File Templates.
 */
public interface IOAuthProvider {
    String getAuthorizationRequestUrl();

    String getName();

    String getParams();

    String getRedirectUrl();

    String getClientId();

    String getOauthTokenLocation();

    String createUserProfileUrl(String accessToken);

    String getClientSecret();

    OAuthUser.OauthOrigin getOrigin();

    Class getAccessTokenResponseClass();

    String getAccessTokenRequestType();

    OAuthProvider.OauthPhase getPhase();

    void setPhase(OAuthProvider.OauthPhase phase);

}
