package uk.gav.GavArqWeb;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

//Defined a stateless session bean.
@Stateless
//This will be a local (running in same server) and 'no interface' (does not implement a bean interface)
//http://piotrnowicki.com/2013/03/defining-ejb-3-1-views-local-remote-no-interface/
@LocalBean
public class LoginBean {
	// Use standard Java logging. Note that as this bean is hosted in container, it it the container
	// logging.properties that will be used rather than that defined by this project.
	private static Logger log = Logger.getLogger(LoginBean.class.getName());
	//NOTE: Credentials here is the ENTITY ... not the table, so needs to be correct case for all fields
	private final static String GET_CRED = "SELECT c FROM Credentials c WHERE c.username = :u";
	
	// Needs to be in a container for a  PersistenceContext to be injected - hence why it cannot be assigned to a @RunOnClient test.
	@PersistenceContext
	private EntityManager em;

	public Credentials findPerson(String user) throws Exception{
		Query q = em.createQuery("SELECT id from Credentials");
		log.fine("LoginBean::FIND PERSON. Rows available in DB::" + q.getResultList().size());

		TypedQuery<Credentials> query = em.createQuery(GET_CRED, Credentials.class);
		// Inject the provided user to entity query.
		query.setParameter("u", user);
		List<Credentials> results = query.getResultList();
				
		if (results.size() > 0) {
			return results.get(0);
		}
		else {
			throw new Exception("User does not exist");
		}
	}
}
