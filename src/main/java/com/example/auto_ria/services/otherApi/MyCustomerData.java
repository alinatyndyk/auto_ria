package com.example.auto_ria.services.otherApi;

public class MyCustomerData {
    private String name;
    private String email;

    public MyCustomerData(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
