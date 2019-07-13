package org.mbari.vars.services.model;

/**
 * @author Brian Schlining
 * @since 2017-05-11T13:37:00
 */
public class User {

    private String username;
    private String firstName;
    private String lastName;
    private String affiliation;
    private String email;
    private String password;
    private String rold;


    public User(String userName, String password, String role, String firstName, String lastName, String affiliation, String email) {
        this.username = userName;
        this.password = password;
        this.rold = role;
        this.firstName = firstName;
        this.lastName = lastName;
        this.affiliation = affiliation;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRold() {
        return rold;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRold(String rold) {
        this.rold = rold;
    }
}
