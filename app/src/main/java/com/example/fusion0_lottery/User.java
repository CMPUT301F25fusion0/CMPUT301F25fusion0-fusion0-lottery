package com.example.fusion0_lottery;

/**
 * This class is a model class that stores
 * users information such as full name, email,
 * phone number,
 * device_id, and their role
 */
public class User {
    public String name;
    private String email;
    private String phone_number;
//
//    private String password;

    private String device_id;
    private String role;

    /**
     * default constructor for Firebase
     */
    public User(){

    }

    public User(String name, String email, String phone_number, String role, String device_id) {
        this.name = name;
        this.email = email;
        this.phone_number = phone_number;
//        this.password = password;
        this.role = role;
        this.device_id = device_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

//    public String getPassword() {
//        return password;
//    }

//    public void setPassword(String password) {
//        this.password = password;
//    }


    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDevice_id(){
        return device_id;
    }

    public  void setDevice_id(String device_id){
        this.device_id = device_id;
    }
}
