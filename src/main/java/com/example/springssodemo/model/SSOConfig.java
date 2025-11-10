package com.example.springssodemo.model;

import java.util.List;

public class SSOConfig {
    private String type;
    private String clientId;
    private String clientSecret;

    private boolean enabled;
    private String loginUrl;        // for JWT
    private String samlSsoUrl;      // for SAML
    private String authUrl;          // for OAuth
    private String redirectUri;      // for OAuth
    private List<String> scopes;     // for OAuth
    private String tokenUrl;         // for OAuth
    private String userInfoUrl;      // for OAuth

    // --- type, clientId, clientSecret ---
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

    // --- additional fields ---
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getLoginUrl() { return loginUrl; }
    public void setLoginUrl(String loginUrl) { this.loginUrl = loginUrl; }

    public String getSamlSsoUrl() { return samlSsoUrl; }
    public void setSamlSsoUrl(String samlSsoUrl) { this.samlSsoUrl = samlSsoUrl; }

    public String getAuthUrl() { return authUrl; }
    public void setAuthUrl(String authUrl) { this.authUrl = authUrl; }

    public String getRedirectUri() { return redirectUri; }
    public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }

    public List<String> getScopes() { return scopes; }
    public void setScopes(List<String> scopes) { this.scopes = scopes; }

    public String getTokenUrl() { return tokenUrl; }
    public void setTokenUrl(String tokenUrl) { this.tokenUrl = tokenUrl; }

    public String getUserInfoUrl() { return userInfoUrl; }
    public void setUserInfoUrl(String userInfoUrl) { this.userInfoUrl = userInfoUrl; }
}
