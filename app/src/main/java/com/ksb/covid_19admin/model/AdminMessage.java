package com.ksb.covid_19admin.model;

public class AdminMessage {

  private String title;
  private String descp; // Description
  private String imageURL;
  private String adminEmail;

    public AdminMessage()
    {

    }

    public AdminMessage(String title, String descp, String imageURL, String adminEmail) {
        this.title = title;
        this.descp = descp;
        this.imageURL = imageURL;
        this.adminEmail = adminEmail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescp() {
        return descp;
    }

    public void setDescp(String descp) {
        this.descp = descp;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }
}
