package uk.gav.GavArqWeb;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@SessionScoped
public class LoginController implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String SUCCESS_MESSAGE = "Welcome";
    private static final String FAILURE_MESSAGE =
        "Incorrect username and password combination";

    private User currentUser;
    private boolean renderedLoggedIn = false;
    
    @Inject
    private Credentials credentials;
    
    public String login() {
        if ("demo".equals(credentials.getUsername()) &&
            "demo".equals(credentials.getPassword())) {
            currentUser = new User("demo");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(SUCCESS_MESSAGE));
            return "home.xhtml";
        }

        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_WARN,
                FAILURE_MESSAGE, FAILURE_MESSAGE));
        return null;
    }
    
    public boolean isRenderedLoggedIn() {
        if(currentUser != null) {
            return renderedLoggedIn;
        } else {
            return false;
        }
    }
    
    public void renderLoggedIn() {
        this.renderedLoggedIn = true;
    }
    
    @Produces
    @Named
    public User getCurrentUser() {
        return currentUser;
    }
}