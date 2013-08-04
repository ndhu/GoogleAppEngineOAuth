

# Exploring OAuth2 on GAE
Explores a _Login with Facebook_ & _Login with Google_ approach for apps that run on Google App Engine or a plain old servlet environment.

GAE offers a UserService, that does leverage Authentication via Google Accounts but not other Providers. This example here uses the open OAuth2 protocol, that is supported by the likes of Facebook, Google, GitHub - no Twitter yet -
and provides a way to authenticate users via their Facebook-, Google- etc. account.

Read [the OAuth Specification](http://oauth.net/) for deeper insights.

## Flow

The flow of an oauth2 authentication goes like this:

+ User clicks the _Login with [PROVIDER]_ Button on your App
+ The App makes an Authorization Request (server side) to the OAuth Provider (e.g. Facebook), passing the client id of your App, a Scope and a redirect url.
    + The client id must be registered at the OAuth Provider and is only known to your App and the provider.
    + The Scope indicates what data (services) your App would like to get access to. For the login flow, basic user info is sufficient.
    + The redirect url is where the provider will send an authorization code if the user granted access or an error code if access was denied.
+ If the user is not yet logged in at the OAuth Provider (e.g. Facebook), the provider handles the login
+ On behalves of your app, the provider (e.g. Facebook) asks the user to grant required permissions to access user specific data.
+ If granted, the provider sends an authorization code to the redirect url provided by the app
+ The app handles the request and extracts the authorization code.
+ The app makes another request to the provider, this time to get an access token, passing the authorization code and a redirect url
+ The provider sends the access token to the redirect url
+ The app handles the request and extracts the access token
+ Once the app has an access token, it can access resources that has been granted, passing the access token.
It can access a resource providing user information to get the user id and name of the user that logged in.
+ Once the app has the user id (at the specific provider) it is easy to map it to local entities in the datastore

See the [Protocol Flow as specified in RFC 6749](http://tools.ietf.org/html/rfc6749#section-1.2)

## Configuration

This demo uses an external config file to configure the access to th OAuth Providers.
The _OAuthProviderService_ (which is also a ServletContextListener) loads the configuration at startup
and creates a List of OAuthProviders, that are then used in the _OAuthServlet_ to help orchestrate the Handshake.

This solution also uses an external file to store client id and client secrets.
At the moment there is no convenient way to store sensitive configuration data in GAE.
I decided to store the values in a config-file (_api-keys-secrets.config_), which of course should not be added to code version control. Still, note,  that anyone with
access to your GAE-App can read this information.
An alternative on GAE might be to use the remote api and store the secrets right in the datastore.

In order to use an OAuth Provider you need to register, configure and obtain a couple of items

### Client ID and Client Secret

Only known to your app and the provider

#### Google

    google.client.id=YOUR_GOOGLE_CLIENT_ID
    google.client.secret=YOUR_GOOGLE_CLIENT_SECRET

configured in _api-keys-secrets.config_

See [Using OAuth2 to access Google APIs](https://developers.google.com/accounts/docs/OAuth2) and the [APIs console](https://code.google.com/apis/console/)
to register and create the id and the secret alongside other data as the redirect urls

#### Facebook

Similar: [Create a Facebook app](https://developers.facebook.com/docs/facebook-login/getting-started-web/)

to obtain an id, secret and register the redirect urls.

### Configuring the Handshake

#### Google

    # resource location for authorization request
    google.oauth.authorize.uri=https://accounts.google.com/o/oauth2/auth

    # scope requested by the app, where access needs to be granted by the provider (user)
    google.oauth.authorize.params=scope=https://www.googleapis.com/auth/userinfo.profile&response_type=code&access_type=offline

    # The redirect location, this location must be registered in your profile in the api console (where you obtained the id and the secret)
    google.oauth.redirect=/oauth/google/redirect/

    # Once we've got the authoriation code we can ask for an access token. This is the url to call for a google oauth2 token
    google.oauth.access.token.uri=https://accounts.google.com/o/oauth2/token

    #Any parameters we want to append, grant_type is mandatory
    google.oauth.access.token.params=grant_type=authorization_code

    # POST-Method expected
    google.oauth.access.token.method=POST

    # The class to use to process the response. Leverages Apache Amber and an appropriate class
    google.oauth.access.token.response.class=org.apache.amber.oauth2.client.response.OAuthJSONAccessTokenResponse

    # Web Service to get basic user info of the user that loggged in (granted access)
    google.oauth.user.api.uri=https://www.googleapis.com/oauth2/v2/userinfo

To lookup the values for the authorization scope you might want to lookup the values at [OAuth2 Playground](https://developers.google.com/oauthplayground/) (google specific)

#### Facebook

Similar

    facebook.oauth.authorize.uri=https://graph.facebook.com/oauth/authorize
    facebook.oauth.authorize.params=scope=user_about_me,user_birthday,user_hometown
    facebook.oauth.redirect=/oauth/facebook/redirect/
    facebook.oauth.access.token.uri=https://graph.facebook.com/oauth/access_token
    facebook.oauth.access.token.params=
    facebook.oauth.access.token.method=POST
    facebook.oauth.access.token.response.class=org.apache.amber.oauth2.client.response.GitHubTokenResponse
    facebook.oauth.user.api.uri=https://graph.facebook.com/me

[Scopes lookup](https://developers.facebook.com/docs/reference/login/#permissions) for scopes to obtain authorization to


## Design

This Demo leverages parts of Apache Amber but abstracts away the configuration of providers and handshakes
in configuration files. It uses plain old servlets and is not dependent on any MVC Framework (although the
OAuthServlet could easily and conveniently be turned into e.g. a Sring MVC-Controller).

The OAuthServlet processes the invocation of the authorization request and handles the redirection callback from the provider.

The following URL-Patterns are mapped to  _OAuthServlet_

    <servlet-mapping>
        <servlet-name>oAuthServlet</servlet-name>
        <url-pattern>/oauth/facebook/invoke/*</url-pattern>
        <url-pattern>/oauth/facebook/redirect/*</url-pattern>
        <url-pattern>/oauth/google/invoke/*</url-pattern>
        <url-pattern>/oauth/google/redirect/*</url-pattern>
    </servlet-mapping>

The _OAuthProviderService_ creates OAuthProviders from the config files at startup (implements ServletContextListener).
It is used to lookup a specific OAuthProvider to use for a given url pattern. All requests and processing of the responses are based on the OAuthProviders.

Once the OAuthServlet has successfully obtained the user information of the current user (via the OAuthProvider)
a OAuthUser is created, that abstracts away any inconsistencies regarding the user info of a specific provider.

After a successful handshake we have an OAuthUser with id, firstName, lastName and name to work with.
We could then use this object to e.g. lookup or create and populate an AppUser Entity in the gae datastore.


This file was modified by IntelliJ IDEA 12.1.4 for binding GitHub repository