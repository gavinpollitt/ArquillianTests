package uk.gav.GavArqWeb;

import java.io.Serializable;
import java.util.logging.Logger;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

// Can access 'loginController' in the faces pages
@Named
@SessionScoped
public class LoginController implements Serializable {
	private static final long serialVersionUID = 1L;

	// Use standard Java logging. Note that as this bean is hosted in container, it it the container
	// logging.properties that will be used rather than that defined by this project.
	private static Logger log = Logger.getLogger(LoginController.class.getName());

    private static final String SUCCESS_MESSAGE = "Welcome";
    private static final String FAILURE_MESSAGE =
        "Incorrect username and password combination";

    private User currentUser;
    private boolean renderedLoggedIn = false;
    
    // From the login.xhtml faces page
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
                    currentUser = new User(credentials.getUsername());
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(SUCCESS_MESSAGE));
                    return "home.xhtml";
                }   		
    	}
    	catch (Exception e) {
    		
    	}
    	
    	log.fine("Credentials supplied: " + credentials);
    	log.fine("Credentials found: " + c);
    	log.fine("login bean: " + loginBean);
    	
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
    
    //Make current user available to faces pages as a managed bean
    @Produces
    @Named
    public User getCurrentUser() {
        return currentUser;
    }
}