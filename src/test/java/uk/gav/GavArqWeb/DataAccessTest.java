package uk.gav.GavArqWeb;

import static org.junit.Assert.assertEquals;

import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * 
 * This will use the default Hibernate Entity Manager to persist data.
 * Not really required in a container, but it was a loose end from another test
 * Will try with Aquillian Extensions in the next test. 
 */
@RunWith(Arquillian.class)
public class DataAccessTest {

	// Use standard Java logging. Note that as this bean is hosted in container, it it the container
	// logging.properties that will be used rather than that defined by this project.
	private static Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final Credentials[] CREDS = {
			new Credentials("demo", "demo123"),
			new Credentials("gavin", "pollitt"),
			new Credentials("eric", "theking") };

	private boolean initialised = false;



	// Didn't really need these other params, but put on there just to show their availability
	@Deployment(name = "persistence", testable = true, order = 1)
	public static Archive<JavaArchive> createJARDeployment() {

		// Note: persistence.xml was a big problem to start with. Needed a test
		// one in test
		// folders as well as the runtime one. Also, as it's a JAR, needed
		// adding in root of
		// manifest. Would have been in 'META-INF' if put in WAR.
		JavaArchive persJar = ShrinkWrap
				.create(JavaArchive.class, "gav.jar")
				.addClasses(Credentials.class)
				.addAsManifestResource("testing-persistence.xml",
						"persistence.xml")
				.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

		log.fine(" Content of SW jar is::" + persJar.toString(true));
		return persJar;
	}

	// As this test will run in the container, the context will be injected.
	@PersistenceContext
	EntityManager em;

	// Same with the transaction
	@Inject
	UserTransaction utx;

	// @Before
	public void preparePersistenceTest() throws Exception {
		if (!initialised) {
			System.out.println("EM IS::" + em);
			System.out.println("UTX IS::" + utx);
			clearData();
			insertData();
			initialised = true;
		}
	}

	private void clearData() throws Exception {
		utx.begin();
		em.joinTransaction();
		System.out.println("Dumping old records...");
		em.createQuery("delete from Credentials").executeUpdate();
		utx.commit();
	}

	private void insertData() throws Exception {
		utx.begin();
		em.joinTransaction();
		System.out.println("Inserting records...");
		for (Credentials c : CREDS) {
			em.persist(c);
		}
		utx.commit();
		// reset the persistence context (cache)
		em.clear();

	}
	
	@Test
	@OperateOnDeployment("persistence")
	public void persist() throws Exception {
		preparePersistenceTest();
		Query q = em.createQuery("SELECT id from Credentials");
		log.info("Rows found in the DB::" + q.getResultList().size());
		assertEquals(3, q.getResultList().size());
	}

}
