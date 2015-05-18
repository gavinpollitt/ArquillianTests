package uk.gav.GavArqWeb;

import java.io.Serializable;

import javax.ejb.EJB;
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
    
    // Changed from @EJB because then '@Specialized' works properly, so mocks can be
    // injected as required. The 'super-class' LoginBean (rather than extending Mock)
    // is now 'hidden' from scope.
    // See http://www.adam-bien.com/roller/abien/entry/inject_vs_ejb
    @Inject
    private LoginBean loginBean;
    
    public String login() {
    	Credentials c = null;
    	try {
    		c = loginBean.findPerson(credentials.getUsername());
    		
            if (c.getPassword().equals(credentials.getPassword())) {
                    currentUser = new User("demo");
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(SUCCESS_MESSAGE));
                    return "home.xhtml";
                }   		
    	}
    	catch (Exception e) {
    		
    	}
    	
    	System.out.println("Credentials supplied: " + credentials);
    	System.out.println("Credentials found: " + c);
    	System.out.println("login bean: " + loginBean);
    	
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