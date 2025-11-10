// ==================== SSOConfigController.java ====================
package com.example.springssodemo.controller;

import com.example.springssodemo.dto.JWTConfigDTO;
import com.example.springssodemo.dto.OAuthConfigDTO;
import com.example.springssodemo.dto.SAMLConfigDTO;
import com.example.springssodemo.model.SSOConfig;   // ✅ Import fixed
import com.example.springssodemo.service.SSOConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sso")
public class SSOConfigController {
    
    private final SSOConfigService ssoConfigService;
    
    public SSOConfigController(SSOConfigService ssoConfigService) {
        this.ssoConfigService = ssoConfigService;
    }
    
    // Get all SSO configurations
    @GetMapping("/config")
    public ResponseEntity<Map<String, SSOConfig>> getAllConfigs() {
        return ResponseEntity.ok(ssoConfigService.getAllConfigs());
    }
    
    // Get enabled SSO methods (for login page)
    @GetMapping("/enabled")
    public ResponseEntity<Map<String, Boolean>> getEnabledMethods() {
        return ResponseEntity.ok(ssoConfigService.getEnabledSSOMethods());
    }
    
    // Save JWT configuration
    @PostMapping("/jwt")
    public ResponseEntity<String> saveJWTConfig(@RequestBody JWTConfigDTO config) {
        try {
            ssoConfigService.saveJWTConfig(config);
            return ResponseEntity.ok("JWT configuration saved successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to save JWT configuration: " + e.getMessage());
        }
    }
    
    // Save SAML configuration
    @PostMapping("/saml")
    public ResponseEntity<String> saveSAMLConfig(@RequestBody SAMLConfigDTO config) {
        try {
            ssoConfigService.saveSAMLConfig(config);
            return ResponseEntity.ok("SAML configuration saved successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to save SAML configuration: " + e.getMessage());
        }
    }
    
    // Save OAuth configuration
    @PostMapping("/oauth")
    public ResponseEntity<String> saveOAuthConfig(@RequestBody OAuthConfigDTO config) {
        try {
            ssoConfigService.saveOAuthConfig(config);
            return ResponseEntity.ok("OAuth configuration saved successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to save OAuth configuration: " + e.getMessage());
        }
    }
}
