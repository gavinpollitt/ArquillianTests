package uk.gav.GavArqWeb;

import javax.enterprise.inject.Model;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

// Model - shorthand for @RequestScoped and @Named. Can now be used by JSFs directly
@Model
// Entity - Maps as a JPA entity to allow automated persistence to data storage.
@Entity
public class Credentials {
	private int	   id;
	private String username;
	private String password;

	public Credentials() {}
	
	public Credentials(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	@Id @GeneratedValue
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

    @NotNull
    @Size(min = 4, max = 50)
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

    @NotNull
    @Size(min = 5, max = 10)
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String toString() {
		return id + "::" + username + ":" + password;
	}
}