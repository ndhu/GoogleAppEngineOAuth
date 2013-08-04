package com.hulstkamp.explore.oauth.com;

import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import org.apache.amber.oauth2.client.OAuthClient;
import org.apache.amber.oauth2.client.URLConnectionClient;
import org.apache.amber.oauth2.client.request.OAuthClientRequest;
import org.apache.amber.oauth2.client.response.OAuthAuthzResponse;
import org.apache.amber.oauth2.client.response.OAuthClientResponse;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.types.GrantType;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;


/**
 * Created with IntelliJ IDEA.
 * User: hulstkan
 * Date: 27.07.13
 * Time: 10:41
 * To change this template use File | Settings | File Templates.
 */
public class OAuthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        //eval the provider that is associated with this request
        IOAuthProvider provider = OAuthProviderService.getInstance().getOauthProviderFor(req.getRequestURI());

        //If we could not eval an associated provider and the phase return an error
        if (provider == null || provider.getPhase().equals(OAuthProvider.OauthPhase.INVALID)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        //use the provider an the evaluated phase to handle the request
        if (provider.getPhase().equals(OAuthProvider.OauthPhase.AUTHORIZATION)) {
            handleAuthorizationPhase(provider, req, resp);
        } else {
            handleAuthorizationResponse(provider, req, resp);
        }
    }

    /**
     * Invokes an Authorization request on a specific OAuth2 Provider
     * @param provider The provider to use for the authorization
     * @param req
     * @param resp
     * @throws IOException
     */
    private void handleAuthorizationPhase(IOAuthProvider provider, HttpServletRequest req, HttpServletResponse resp) throws IOException {

        try {
            String uri = buildOauthRequest(provider);
            resp.sendRedirect(uri);
        } catch (OAuthSystemException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    /**
     * Called by the provider when a User has given authorization to the client-app (us)
     * @param provider the provider used for authorization
     * @param req
     * @param resp
     * @throws IOException
     */
    private void handleAuthorizationResponse(IOAuthProvider provider, HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

        OAuthClientResponse oAuthResponse = requestAccessToken(provider, req, resp);

        if (oAuthResponse != null) {
            String accessToken = oAuthResponse.getParam("access_token");
            /*
             * you might want to get these as well
             *   String refreshToken = oAuthResponse.getRefreshToken();
             *   String scope = oAuthResponse.getScope();
             *   Long expiresIn = oAuthResponse.getExpiresIn();
             *   String expires = oAuthResponse.getParam("expires");
             *   String body = oAuthResponse.getBody();
             *   String id = oAuthResponse.getParam("id");
             */
            JSONObject jsonUserProfile = fetchUserProfile(provider, accessToken);
            OAuthUser user = createOauthUser(provider, jsonUserProfile);

            UUID uuid = UUID.randomUUID();
            String hashedId = createHash(user.getId());

            req.setAttribute("id", user.getId());
            req.setAttribute("firstName", user.getFirstName());
            req.setAttribute("lastName", user.getLastName());
            req.setAttribute("name", user.getName());
            req.getRequestDispatcher("/WEB-INF/userInfo.jsp").forward(req, resp);
        }
        else {
            req.getRequestDispatcher("/WEB-INF/oauthError.jsp").forward(req, resp);
        }

        /*
        final PrintWriter writer = resp.getWriter();
        writer.append("<section class='info'>");
        writer.append(user.toString());
        writer.append("</section>");
        writer.close();
        */
    }

    private String createHash(String value)  {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
            byte [] digest = md.digest(value.getBytes("UTF-8"));
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < digest.length; i++) {
                sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    private String buildOauthRequest(IOAuthProvider provider) throws OAuthSystemException {

        /*
         * Facebook OAuth login
         * https://developers.facebook.com/docs/reference/dialogs/oauth/
         *
         * Permission names
         * https://developers.facebook.com/docs/reference/login/#permissions
         *
         * http://googlecodesamples.com/oauth_playground/
         */
        OAuthClientRequest request = OAuthClientRequest
                .authorizationLocation(provider.getAuthorizationRequestUrl())
                .setClientId(provider.getClientId())
                .setRedirectURI(provider.getRedirectUrl())
                .buildQueryMessage();

        return request.getLocationUri();
    }



    private OAuthClientRequest buildOauthAccessTokenRequest(IOAuthProvider provider, String code) {
        OAuthClientRequest request = null;
        try {
            request = OAuthClientRequest
                    .tokenLocation(provider.getOauthTokenLocation())
                    .setGrantType(GrantType.AUTHORIZATION_CODE)
                    .setClientId(provider.getClientId())
                    .setClientSecret(provider.getClientSecret())
                    .setRedirectURI(provider.getRedirectUrl())
                    .setCode(code)
                    .buildBodyMessage();

        } catch (OAuthSystemException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return request;
    }

    private OAuthClientResponse requestAccessToken(IOAuthProvider provider, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        OAuthAuthzResponse oar = null;
        try {
            oar = OAuthAuthzResponse.oauthCodeAuthzResponse(req);
        } catch (OAuthProblemException e) {
            e.printStackTrace();

            return null;
        }

        String state = oar.getState();

        //Build a request using the code just returned, the client code and the client id to get an access token
        //for the api.
        OAuthClientRequest request = buildOauthAccessTokenRequest(provider, oar.getCode());

        //create OAuth client that uses custom http client under the hood
        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

        //Facebook is not fully compatible with OAuth 2.0 draft 10, access token response is
        //application/x-www-form-urlencoded, not json encoded so we use dedicated response class for that
        //Custom response classes are an easy way to deal with oauth providers that introduce modifications to
        //OAuth 2.0 specification


        OAuthClientResponse oAuthResponse = null;
        try {
            oAuthResponse = oAuthClient.accessToken(request, provider.getAccessTokenRequestType(), provider.getAccessTokenResponseClass());
            //oAuthResponse = oAuthClient.accessToken(request, GitHubTokenResponse.class);
        } catch (OAuthSystemException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (OAuthProblemException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return oAuthResponse;
    }

    /**
     * Fetches properties of the user profile that have been given permission via scope.
     * Uses the access token we obtained to invoke the api
     * <p/>
     * Check
     * Facebook API Explorer
     * https://developers.facebook.com/tools/explorer
     * <p/>
     * To get the user id call
     * https://graph.facebook.com/me?access_token=
     * <p/>
     * The specification of the returned object can be found at
     * https://developers.facebook.com/docs/reference/api/user/
     *
     * @param accessToken
     * @return
     * @throws IOException
     */
    private JSONObject fetchUserProfile(IOAuthProvider provider, String accessToken) throws IOException {

        final URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
        String fetchUserUrl = provider.createUserProfileUrl(accessToken);
        URL url = new URL(fetchUserUrl);

        //convert the payload to json for convenience
        HTTPResponse urlFetchResponse = urlFetchService.fetch(url);
        byte[] contentBytes = urlFetchResponse.getContent();
        String contentAsString = new String(contentBytes, "UTF-8"); //.replaceAll("\n", "\\n");
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(contentAsString);
        } catch (com.google.appengine.labs.repackaged.org.json.JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return jsonObject;
    }

    /**
     * Creates an OAuthUser form a jsonUserProfile
     * The OAuthUser is a normalized User that reflects
     * a user profile from different oauth providers
     *
     * @param jsonUserProfile
     * @return
     */
    private OAuthUser createOauthUser(IOAuthProvider provider, JSONObject jsonUserProfile) {
        OAuthUser user = null;
        try {
            user = OAuthUser.create(jsonUserProfile, provider.getOrigin());
        } catch (com.google.appengine.labs.repackaged.org.json.JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return user;
    }


}
