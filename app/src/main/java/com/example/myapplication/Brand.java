package com.example.myapplication;

public class Brand {
    private String _id;
    private String name;
    private String category;
    private String investment;
    private int logoResId;
    private String logoUrl;
    private String ownerEmail;
    private String ownerMobile;

    public Brand(String name, String category, String investment, int logoResId) {
        this.name = name;
        this.category = category;
        this.investment = investment;
        this.logoResId = logoResId;
    }

    public Brand(String name, String category, String investment, String logoUrl) {
        this.name = name;
        this.category = category;
        this.investment = investment;
        this.logoUrl = logoUrl;
    }

    public Brand(String name, String category, String investment, String logoUrl, String ownerEmail, String ownerMobile) {
        this.name = name;
        this.category = category;
        this.investment = investment;
        this.logoUrl = logoUrl;
        this.ownerEmail = ownerEmail;
        this.ownerMobile = ownerMobile;
    }

    public String getId() { return _id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getInvestment() { return investment; }
    public int getLogoResId() { return logoResId; }
    public String getLogoUrl() { return logoUrl; }
    public String getOwnerEmail() { return ownerEmail; }
    public String getOwnerMobile() { return ownerMobile; }
}
