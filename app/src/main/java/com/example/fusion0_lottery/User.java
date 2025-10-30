package com.example.fusion0_lottery;

/**
 * This class is a model class that stores
 * users information such as full name, email,
 * phone number,
 * device_id, and their role
 */
public class User {
    public String full_name;
    private String email;
    private String phone_number;
    private String device_id;
    private String role;

    /**
     * default constructor for Firebase
     */
    public User(){

    }

    public User(String device_id,String full_name, String email, String phone_number, String role) {
        this.full_name = full_name;
        this.email = email;
        this.phone_number = phone_number;
        this.device_id = device_id;
        this.role = role;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getEmail() {
        return email;
    }
    public String getDevice_id(){
        return device_id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }
    public  void setDevice_id(String device_id){
        this.device_id = device_id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
