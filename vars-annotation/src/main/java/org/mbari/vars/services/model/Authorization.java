package org.mbari.vars.services.model;

/**
 * @author Brian Schlining
 * @since 2017-05-23T10:16:00
 */
public class Authorization {
    private String tokenType;
    private String accessToken;

    public Authorization(String tokenType, String accessToken) {
        this.tokenType = tokenType;
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public String toString() {
        return tokenType + " " + accessToken;
    }
}
