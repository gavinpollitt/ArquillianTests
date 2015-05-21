package uk.gav.GavArqWeb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.persistence.Cleanup;
import org.jboss.arquillian.persistence.TestExecutionPhase;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * 
 * This will use the default Hibernate Entity Manager to persist data, but directly
 * from container, not using UI to drive this test.
 */
@RunWith(Arquillian.class)
public class BeanandExtensionAccessTest {

	// Use standard Java logging. Note that as this bean is hosted in container, it it the container
	// logging.properties that will be used rather than that defined by this project.
	private static Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

	// Use the true LoginBean for this test.
	@Deployment
	public static Archive<JavaArchive> createJARDeployment() {

		// Note: persistence.xml was a big problem to start with. Needed a test
		// one in test
		// folders as well as the runtime one. Also, as it's a JAR, needed
		// adding in root of
		// manifest. Would have been in 'META-INF' if put in WAR.
		JavaArchive persJar = ShrinkWrap
				.create(JavaArchive.class, "gav.jar")
				.addClasses(Credentials.class, LoginBean.class)
				.addAsManifestResource("testing-persistence.xml",
						"persistence.xml")
				.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

		log.fine("Content of SW jar is::" + persJar.toString(true));
		return persJar;
	}

	@PersistenceContext()
	EntityManager em;

	@Inject
	UserTransaction utx;

	@EJB
	LoginBean loginBean;
	
	// No 'CleanUp' strategy defined because TestExecutionPhase.NONE means it's ignored.
	// i.e. we want to keep the data seeded for all of our tests
	// Don't really need this test, if we have only a single test or tests are all read-only
	// but just put in to demonstrate an initial set-up
	@Test
	@InSequence(1)
    @UsingDataSet("credentials.yml")
	@Cleanup(phase=TestExecutionPhase.NONE)
	public void persist() throws Exception {
		Query q = em.createQuery("SELECT id from Credentials");
		log.info("persist:::Rows now in the DB::" + q.getResultList().size());
		assertEquals(3, q.getResultList().size());
	}
	
	//Clean the data after this to try next test...
	@Test
	@InSequence(2)
	@Cleanup(phase=TestExecutionPhase.AFTER)
	public void testGoodCredentials() throws Exception {
		Credentials c = loginBean.findPerson("gavin");
		assertEquals("pollitt", c.getPassword());
		c = loginBean.findPerson("demo");
		assertEquals("demo123", c.getPassword());
		c = loginBean.findPerson("eric");
		assertNotEquals("longlivetheking", c.getPassword());
	}
	
	// Note, that this test will pass if the Exception is thrown, hence the 'expected' property.
	@Test(expected=Exception.class)
	@InSequence(3)
	public void testBadCredentials() throws Exception {
		Credentials c = loginBean.findPerson("gavin");
		assertEquals("pollitt", c.getPassword());
	}	

}
