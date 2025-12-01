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

    private String device_id;
    private String role;

    /**
     * default constructor for Firebase
     */
    public User(){

    }

    /**
     * Constructs a new User with all fields.
     *
     * @param name full name
     * @param email email address
     * @param phone_number phone number
     * @param role user role
     * @param device_id device identifier
     */
    public User(String name, String email, String phone_number, String role, String device_id) {
        this.name = name;
        this.email = email;
        this.phone_number = phone_number;
        this.role = role;
        this.device_id = device_id;
    }

    /** @return the full name of the user */
    public String getName() {
        return name;
    }

    /** @param name sets the user's full name */
    public void setName(String name) {
        this.name = name;
    }

    /** @return the user's email */
    public String getEmail() {
        return email;
    }

    /** @param email sets the user's email */
    public void setEmail(String email) {
        this.email = email;
    }

    /** @return the user's phone number */
    public String getPhone_number() {
        return phone_number;
    }

    /** @param phone_number sets the user's phone number */
    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    /** @return the user's role */
    public String getRole() {
        return role;
    }

    /** @param role sets the user's role */
    public void setRole(String role) {
        this.role = role;
    }

    /** @return the user's device ID */
    public String getDevice_id(){
        return device_id;
    }

    /** @param device_id sets the user's device ID */
    public  void setDevice_id(String device_id){
        this.device_id = device_id;
    }
}