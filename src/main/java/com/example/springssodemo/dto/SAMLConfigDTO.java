package com.example.springssodemo.dto;

public class SAMLConfigDTO {
    private String idpEntityId;
    private String ssoUrl;
    private String certificate;

    public String getIdpEntityId() {
        return idpEntityId;
    }
    public void setIdpEntityId(String idpEntityId) {
        this.idpEntityId = idpEntityId;
    }

    public String getSsoUrl() {
        return ssoUrl;
    }
    public void setSsoUrl(String ssoUrl) {
        this.ssoUrl = ssoUrl;
    }

    public String getCertificate() {
        return certificate;
    }
    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }
}
