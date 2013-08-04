package com.hulstkamp.explore.oauth.com;

/**
 * Describes an OAuthProvider.
 */
public interface IOAuthProvider {

    /**
     * Returns the URI for the authentication request
     * @return URI
     */
    String getAuthorizationRequestUrl();

    /**
     * Returns the URI for the redirect, where the provider will send the code | error after authorization
     * @return
     */
    String getRedirectUrl();

    /**
     * Returns the client id
     * @return client id
     */
    String getClientId();

    /**
     * Returns the client secret
     * @return  client secret
     */
    String getClientSecret();

    /**
     * Returns the URI for fetching the access token
     * @return URI for access token
     */
    String getOauthTokenLocation();

    /**
     * Returns the URI to get the user profile
     * @param accessToken  to use for the request
     * @return JSON holding user info
     */
    String createUserProfileUrl(String accessToken);

    /**
     * The Origin of the Provider
     * @return
     */
    OAuthUser.OauthOrigin getOrigin();

    /**
     * Returns the class to use to handle the access token response
     * @return the Class
     */
    Class getAccessTokenResponseClass();

    /**
     * The Request Type to get the access token
     * @return access token request type GET | POST
     */
    String getAccessTokenRequestType();

    /**
     * Returns the phase the OAuthProvider is in
     * @return  the current phase
     */
    OAuthProvider.OauthPhase getPhase();

    void setPhase(OAuthProvider.OauthPhase phase);

}
