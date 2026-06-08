package com.example.myapplication;

public class User {
    private String firstName;
    private String middleName;
    private String lastName;
    private String dob;
    private String city;
    private String pincode;
    private String address;
    private String occupation;
    private String mobile;
    private String email;
    private String password;
    private String gender;
    private String state;
    private String qualification;

    // For Registration
    public User(String firstName, String middleName, String lastName, String dob, String city, String pincode, 
                String address, String occupation, String mobile, String email, String password, 
                String gender, String state, String qualification) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.dob = dob;
        this.city = city;
        this.pincode = pincode;
        this.address = address;
        this.occupation = occupation;
        this.mobile = mobile;
        this.email = email;
        this.password = password;
        this.gender = gender;
        this.state = state;
        this.qualification = qualification;
    }

    // For Login
    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getFirstName() { return firstName; }
    public String getMiddleName() { return middleName; }
    public String getLastName() { return lastName; }
    public String getDob() { return dob; }
    public String getCity() { return city; }
    public String getPincode() { return pincode; }
    public String getAddress() { return address; }
    public String getOccupation() { return occupation; }
    public String getMobile() { return mobile; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getGender() { return gender; }
    public String getState() { return state; }
    public String getQualification() { return qualification; }

    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setDob(String dob) { this.dob = dob; }
    public void setCity(String city) { this.city = city; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    public void setAddress(String address) { this.address = address; }
    public void setOccupation(String occupation) { this.occupation = occupation; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setGender(String gender) { this.gender = gender; }
    public void setState(String state) { this.state = state; }
    public void setQualification(String qualification) { this.qualification = qualification; }
}
