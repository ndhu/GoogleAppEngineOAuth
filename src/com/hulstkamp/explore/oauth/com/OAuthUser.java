package com.hulstkamp.explore.oauth.com;

import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * OAuthUser: hulstkan
 * Date: 27.07.13
 * Time: 08:25
 * To change this template use File | Settings | File Templates.
 */
public class OAuthUser {

    public enum OauthOrigin {

        FACEBOOK("FACEBOOK", PropertiesKeys.FACEBOOK),
        GOOGLE("GOOGLE", PropertiesKeys.GOOGLE),
        HOME("HOME", PropertiesKeys.HOME);

        private final String originName;
        private final PropertiesKeys propertiesKeys;

        OauthOrigin(final String originName, final PropertiesKeys propertiesKeys) {
            this.originName = originName;
            this.propertiesKeys = propertiesKeys;
        }

        public String getOriginName() {
            return this.originName;
        }

        public PropertiesKeys getPropertiesKeys() {
            return this.propertiesKeys;
        }

        public String getKeyName(PropertyKeyName keyName) {
            return propertiesKeys.get(keyName);
        }

        public static OauthOrigin getOriginByName(String providerName) {
            OauthOrigin[] origins = OauthOrigin.values();
            for (OauthOrigin origin : origins) {
                if (origin.getOriginName().toLowerCase().equals(providerName.toLowerCase())) {
                    return origin;
                }
            }
            return null;
        }

        public enum PropertyKeyName {
            ID,
            FIRST_NAME,
            LAST_NAME,
            NAME
        }

        /**
         *  Defines properties Key Names for a specific origin
         */
        private enum PropertiesKeys {
            FACEBOOK("id", "first_name", "last_name", "name"),
            GOOGLE("id", "given_name", "family_name", "name"),
            HOME("id", "firstName", "lastName", "name");

            //map that holds the key name of a specific property regarding the origin
            private HashMap<PropertyKeyName, String> keyNamesMap = new HashMap<PropertyKeyName, String>(PropertyKeyName.values().length);

            /**
             *
             * @param id        the name of the key for the field holding the id
             * @param firstName the name of the key for the field holding the first name
             * @param lastName  the name of the key for the field holding the last name
             * @param name      the name of the key for the field holding the name
             */
            private PropertiesKeys (String id, String firstName, String lastName, String name) {
                this.keyNamesMap.put(PropertyKeyName.ID, id);
                this.keyNamesMap.put(PropertyKeyName.FIRST_NAME, firstName);
                this.keyNamesMap.put(PropertyKeyName.LAST_NAME, lastName);
                this.keyNamesMap.put(PropertyKeyName.NAME, name);
            }

            /**
             * Get the keyName for a specific property in the scope of this Origin
             * @param keyName
             * @return
             */
            public String get(PropertyKeyName keyName) {
                return this.keyNamesMap.get(keyName);
            }

        }
    }

    private String originName;
    private String id;
    private String firstName;
    private String lastName;
    private String name;

    private OAuthUser() {
        super();
    }

    public static OAuthUser create(JSONObject properties, OauthOrigin origin) throws JSONException {
        OAuthUser user = new OAuthUser();
        user.originName = origin.getOriginName();
        user.id = properties.getString(origin.getKeyName(OauthOrigin.PropertyKeyName.ID));
        user.firstName = properties.getString(origin.getKeyName(OauthOrigin.PropertyKeyName.FIRST_NAME));
        user.lastName = properties.getString(origin.getKeyName(OauthOrigin.PropertyKeyName.LAST_NAME));
        user.name = properties.getString(origin.getKeyName(OauthOrigin.PropertyKeyName.NAME));
        return user;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id: ").append(this.id).append("\n");
        sb.append("first name: ").append(this.firstName).append("\n");
        sb.append("last name: ").append(this.lastName).append("\n");
        sb.append("name: ").append(this.name).append("\n");
        return sb.toString();
    }
}
