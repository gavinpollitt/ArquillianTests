package uk.gav.GavArqWeb;

import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

@Stateless
@LocalBean
public class LoginBean {
	//NOTE: Credentials here is the ENTITY ... not the table, so needs to be correct case for all fields
	private final static String GET_CRED = "SELECT c FROM Credentials c WHERE c.username = :u";
	
	@PersistenceContext
	private EntityManager em;

	public Credentials findPerson(String user) throws Exception{
		Query q = em.createQuery("SELECT id from Credentials");
		System.out.println("FIND PERSON rows in DB::" + q.getResultList().size());

		TypedQuery<Credentials> query = em.createQuery(GET_CRED, Credentials.class);
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
