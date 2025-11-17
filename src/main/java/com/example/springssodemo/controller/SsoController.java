package com.example.springssodemo.controller;

import com.example.springssodemo.model.SSOConfig;
import com.example.springssodemo.model.User;
import com.example.springssodemo.repo.UserRepository;
import com.example.springssodemo.service.SSOConfigService;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Map;
import java.util.Optional;

@Controller
public class SsoController {

    private final UserRepository userRepository;
    private final SSOConfigService ssoConfigService;
    private final RestTemplate restTemplate;

    public SsoController(UserRepository userRepository, SSOConfigService ssoConfigService) {
        this.userRepository = userRepository;
        this.ssoConfigService = ssoConfigService;
        this.restTemplate = new RestTemplate();
    }

    // ==================== JWT SSO ====================
    
    @GetMapping("/sso/jwt/login")
    public String jwtSsoLogin() {
        Optional<SSOConfig> configOpt = ssoConfigService.getConfigByType("JWT");
        if (configOpt.isEmpty() || !configOpt.get().isEnabled()) {
            return "redirect:/login?error=jwt_not_configured";
        }
        SSOConfig config = configOpt.get();
        String loginUrl = config.getLoginUrl();
        if (loginUrl == null || loginUrl.isBlank()) {
            return "redirect:/login?error=jwt_url_missing";
        }
        return "redirect:" + loginUrl;
    }

    @GetMapping("/sso/jwt/callback")
    public String jwtCallback(@RequestParam(required = false) String id_token,
                              @RequestParam(required = false) String token,
                              HttpSession session) {
        try {
            String jwtRaw = id_token != null ? id_token : token;
            if (jwtRaw == null) return "redirect:/login?error=no_token";

            SignedJWT signedJWT = SignedJWT.parse(jwtRaw);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            String username = extractUsername(claims);
            String email = claims.getStringClaim("email");

            Optional<User> optionalUser = userRepository.findByUsername(username);
            if (optionalUser.isEmpty()) {
                User newUser = new User(username, "<sso>", email == null ? username + "@sso.com" : email, "USER");
                userRepository.save(newUser);
            }

            session.setAttribute("username", username);
            session.setAttribute("authenticated", true);
            session.setAttribute("role", optionalUser.map(User::getRole).orElse("USER"));

            return "redirect:/home";
        } catch (ParseException e) {
            return "redirect:/login?error=invalid_token";
        } catch (Exception e) {
            return "redirect:/login?error=jwt_error";
        }
    }

    // ==================== SAML SSO ====================
    
    @GetMapping("/sso/saml/login")
    public String samlSsoLogin() {
        Optional<SSOConfig> configOpt = ssoConfigService.getConfigByType("SAML");
        if (configOpt.isEmpty() || !configOpt.get().isEnabled()) {
            return "redirect:/login?error=saml_not_configured";
        }
        SSOConfig config = configOpt.get();
        String samlSsoUrl = config.getSamlSsoUrl();
        if (samlSsoUrl == null || samlSsoUrl.isBlank()) {
            return "redirect:/login?error=saml_url_missing";
        }
        return "redirect:" + samlSsoUrl;
    }

    @GetMapping("/sso/saml/callback")
    public String samlCallback(@RequestParam(required = false) String SAMLResponse,
                               HttpSession session) {
        try {
            if (SAMLResponse == null || SAMLResponse.isBlank()) return "redirect:/login?error=no_saml_response";

            String username = extractUsernameFromSAML(SAMLResponse);
            String email = username + "@saml.com";

            Optional<User> optionalUser = userRepository.findByUsername(username);
            if (optionalUser.isEmpty()) {
                User newUser = new User(username, "<sso>", email, "USER");
                userRepository.save(newUser);
            }

            session.setAttribute("username", username);
            session.setAttribute("authenticated", true);
            session.setAttribute("role", optionalUser.map(User::getRole).orElse("USER"));

            return "redirect:/home";
        } catch (Exception e) {
            return "redirect:/login?error=saml_error";
        }
    }

    // ==================== OAuth SSO ====================
    
    @GetMapping("/sso/oauth/login")
    public String oauthSsoLogin() {
        Optional<SSOConfig> configOpt = ssoConfigService.getConfigByType("OAUTH");
        if (configOpt.isEmpty() || !configOpt.get().isEnabled()) {
            return "redirect:/login?error=oauth_not_configured";
        }

        SSOConfig config = configOpt.get();
        String authUrl = config.getAuthUrl();
        String clientId = config.getClientId();
        String redirectUri = config.getRedirectUri();

        // FIX: convert List<String> to comma-separated string
        String scopes = (config.getScopes() != null && !config.getScopes().isEmpty())
                ? String.join(",", config.getScopes())
                : "openid,profile,email";

        if (authUrl == null || clientId == null || redirectUri == null) {
            return "redirect:/login?error=oauth_incomplete_config";
        }

        try {
            String authorizationUrl = authUrl
                    + "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                    + "&response_type=code"
                    + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                    + "&scope=" + URLEncoder.encode(scopes.replace(",", " "), StandardCharsets.UTF_8);

            return "redirect:" + authorizationUrl;
        } catch (Exception e) {
            return "redirect:/login?error=oauth_url_error";
        }
    }

    @GetMapping("/sso/oauth/callback")
    public String oauthCallback(@RequestParam(required = false) String code,
                                @RequestParam(required = false) String error,
                                HttpSession session) {
        try {
            if (error != null) return "redirect:/login?error=oauth_" + error;
            if (code == null) return "redirect:/login?error=no_code";

            Optional<SSOConfig> configOpt = ssoConfigService.getConfigByType("OAUTH");
            if (configOpt.isEmpty()) return "redirect:/login?error=oauth_not_configured";

            SSOConfig config = configOpt.get();
            String accessToken = exchangeCodeForToken(code, config);
            if (accessToken == null) return "redirect:/login?error=token_exchange_failed";

            Map<String, Object> userInfo = getUserInfo(accessToken, config);
            if (userInfo == null) return "redirect:/login?error=userinfo_failed";

            String username = extractUsernameFromUserInfo(userInfo);
            String email = (String) userInfo.getOrDefault("email", username + "@oauth.com");

            Optional<User> optionalUser = userRepository.findByUsername(username);
            if (optionalUser.isEmpty()) {
                User newUser = new User(username, "<sso>", email, "USER");
                userRepository.save(newUser);
            }

            session.setAttribute("username", username);
            session.setAttribute("authenticated", true);
            session.setAttribute("role", optionalUser.map(User::getRole).orElse("USER"));

            return "redirect:/home";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/login?error=oauth_error";
        }
    }

    // ==================== Helper Methods ====================
    
    private String extractUsername(JWTClaimsSet claims) throws ParseException {
        String username = claims.getStringClaim("preferred_username");
        if (username == null) username = claims.getStringClaim("email");
        if (username == null) username = claims.getSubject();
        if (username == null) username = "sso-user-" + System.currentTimeMillis();
        return username;
    }

    private String extractUsernameFromSAML(String samlResponse) {
        return "saml-user-" + System.currentTimeMillis();
    }

    private String exchangeCodeForToken(String code, SSOConfig config) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "authorization_code");
            body.add("code", code);
            body.add("client_id", config.getClientId());
            body.add("client_secret", config.getClientSecret());
            body.add("redirect_uri", config.getRedirectUri());

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(config.getTokenUrl(), request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("access_token");
            }

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Map<String, Object> getUserInfo(String accessToken, SSOConfig config) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    config.getUserInfoUrl(),
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String extractUsernameFromUserInfo(Map<String, Object> userInfo) {
        String username = (String) userInfo.get("preferred_username");
        if (username == null) username = (String) userInfo.get("email");
        if (username == null) username = (String) userInfo.get("sub");
        if (username == null) username = (String) userInfo.get("name");
        if (username == null) username = "oauth-user-" + System.currentTimeMillis();
        return username;
    }
}
