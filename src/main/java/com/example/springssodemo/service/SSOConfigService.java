package com.example.springssodemo.service;

import com.example.springssodemo.dto.JWTConfigDTO;
import com.example.springssodemo.dto.SAMLConfigDTO;
import com.example.springssodemo.dto.OAuthConfigDTO;
import com.example.springssodemo.model.SSOConfig;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SSOConfigService {

    private final Map<String, SSOConfig> configs = new HashMap<>();

    // ✅ Return all configs
    public Map<String, SSOConfig> getAllConfigs() {
        return configs;
    }

    // ✅ Return enabled methods as Map<String, Boolean>
    public Map<String, Boolean> getEnabledSSOMethods() {
        Map<String, Boolean> enabled = new HashMap<>();
        for (String type : List.of("JWT", "SAML", "OAUTH")) {
            enabled.put(type, configs.containsKey(type));
        }
        return enabled;
    }

    // ✅ Save JWT Config
    public void saveJWTConfig(JWTConfigDTO jwtConfig) {
        SSOConfig config = new SSOConfig();
        config.setType("JWT");
        config.setClientId(jwtConfig.getIssuer());          // Updated
        config.setClientSecret(jwtConfig.getSecretKey());   // Updated
        config.setLoginUrl(jwtConfig.getTokenEndpoint());   // Optional mapping
        configs.put("JWT", config);
    }

    // ✅ Save SAML Config
    public void saveSAMLConfig(SAMLConfigDTO samlConfig) {
        SSOConfig config = new SSOConfig();
        config.setType("SAML");
        config.setClientId(samlConfig.getIdpEntityId());   // Updated
        config.setClientSecret(samlConfig.getSsoUrl());
        config.setSamlSsoUrl(samlConfig.getSsoUrl());
        configs.put("SAML", config);
    }

    // ✅ Save OAuth Config
    public void saveOAuthConfig(OAuthConfigDTO oauthConfig) {
        SSOConfig config = new SSOConfig();
        config.setType("OAUTH");
        config.setClientId(oauthConfig.getClientId());
        config.setClientSecret(oauthConfig.getClientSecret());
        config.setRedirectUri(oauthConfig.getRedirectUri());
        configs.put("OAUTH", config);
    }

    // ✅ Return Optional<SSOConfig>
    public Optional<SSOConfig> getConfigByType(String type) {
        return Optional.ofNullable(configs.get(type.toUpperCase()));
    }
}
