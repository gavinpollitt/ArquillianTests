package uk.gav.GavArqWeb;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Specializes;

// Specializes - will ensure that the Mock is injected where available rather than non-mock
// Also need to define as an EJB as the extendee is also an EJB
@Stateless
@LocalBean
@Specializes
@Alternative
public class MockLoginBean extends LoginBean {

	private static final Map<String,String> CREDS = new HashMap<String,String>(3);

	// Use standard Java logging. Note that as this bean is hosted in container, it it the container
	// logging.properties that will be used rather than that defined by this project.
	private static Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

	
	static {
		CREDS.put("demo", "demo123");
		CREDS.put("gavin", "pollitt");
		CREDS.put("eric", "theking");
	}

	public Credentials findPerson(String user) throws Exception{
		log.info("MOCK:FIND PERSON rows in DB::" + CREDS.size());

		
		if (CREDS.get(user) != null) {
			return new Credentials(user, CREDS.get(user));
		}
		else {
			throw new Exception("User does not exist");
		}
	}
}
