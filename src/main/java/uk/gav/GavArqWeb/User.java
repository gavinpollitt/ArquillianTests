package uk.gav.GavArqWeb;

/**
 * 
 * @author gavin
 * Utility bean to identify the logged-in user. Will be '@Produced'
 */
public class User {
    private String username;

    public User() {}
    
    public User(String username) {
        this.username = username;
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}