package uk.gav.GavArqWeb;

import static org.jboss.arquillian.graphene.Graphene.guardAjax;
import static org.jboss.arquillian.graphene.Graphene.guardHttp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.logging.Logger;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.persistence.Cleanup;
import org.jboss.arquillian.persistence.TestExecutionPhase;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * 
 * @author gavin
 *
 *         'browser' will be injected with the appropriate selenium-based browser wrapper
 *         implementation taken from arquillian.xml config. 
 *         'deploymentUrl' wil be injected with the initial URL context. 
 *         The 'FindBy' annotations will inject the corresponding variables with the corresponding
 *         elements from the html rendered
 * 
 */
@RunWith(Arquillian.class)
public class LoginScreenGrapheneTest {

	// Use standard Java logging. Note that as this bean is hosted in container, it it the container
	// logging.properties that will be used rather than that defined by this project.
	private static Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

	private static final String WEBAPP_SRC = "src/main/webapp";

	@FindBy
	private WebElement userName;

	@FindBy
	private WebElement password;

	@FindBy(id = "login")
	private WebElement loginButton;

	// 2. injects a first element with given tag name
	@FindBy(tagName = "li")
	private WebElement facesMessage;

	// 3. injects an element using jQuery selector
	@FindByJQuery("p:visible")
	private WebElement signedAs;

	@FindBy(css = "input[type=submit]")
	private WebElement whoAmI;

	@Deployment(name = "login")
	public static Archive<WebArchive> createWARDeployment() {
		// Create the web archive to test. No Mock added this time around
		WebArchive war = ShrinkWrap
				.create(WebArchive.class, "login.war")
				.addClasses(Credentials.class, User.class,
						LoginController.class, LoginBean.class)
				.addAsWebResource(new File(WEBAPP_SRC, "login.xhtml"))
				.addAsWebResource(new File(WEBAPP_SRC, "home.xhtml"))
				.addAsWebResource(new File(WEBAPP_SRC, "index.html"))
				.addAsResource("testing-persistence.xml",
						"META-INF/persistence.xml")
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
				// Always need at least this minimum file for a faces deployment
				.addAsWebInfResource(
						new StringAsset("<faces-config version=\"2.0\"/>"),
						"faces-config.xml");

		log.fine("WAR Content is::" + war.toString(true));
		return war;
	}

	// JAR file to 'gently persuade' Arquillian to perform an initial data set-up using
	// the Arquillian persistence extensions.
	// Note the addition of the Selenium packages. This overcomes the odd issues where the
	// setup test is expecting the WebDriver class for some reason even though it's not used
	// and is injected directly into the other test.
	@Deployment(name = "setup")
	public static Archive<JavaArchive> createJARDeployment() {
		// Create the web archive to test
		JavaArchive jar = ShrinkWrap
				.create(JavaArchive.class, "setup.jar")
				.addClasses(Credentials.class)
				.addPackage("org.openqa.selenium")
				.addPackage("org.openqa.selenium.internal")
				.addAsManifestResource("testing-persistence.xml",
						"persistence.xml")
				.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

		log.fine("JAR Content is::" + jar.toString(true));
		return jar;
	}

	// Note 'artistic license' as 'UsingDataSet' doesn't work for 'RunAsClient'
	// For some reason needs the WebDriver class still injecting hence its presence
	// in the deployment
	@Test
	@InSequence(1)
	@UsingDataSet("credentials.yml")
	@OperateOnDeployment("setup")
	@Cleanup(phase = TestExecutionPhase.NONE) //Could go on class as well
	public void populateDatabaseWorkaround() throws Exception {
		// Populates database since unable to use @UsingDataSet with
		// @RunAsClient
		// Check https://issues.jboss.org/browse/ARQ-1077
		log.info("Database data initialised in advance of tests");
	}

	// Inject the WebDriver to allow interactions with the desired browser (as specified in
	// arquillian.xml. @Drone manages the Webdriver within the Arquillian framework and is 
	// explained in details at: https://docs.jboss.org/author/display/ARQ/Drone
	// It also utilises Arquillian Graphene to manage the the injection of pages, AJAX etc.
	// The Graphene 2 project is designed as set of extensions for Selenium WebDriver project focused 
	// on rapid development and usability in Java environment
	// See https://docs.jboss.org/author/display/ARQGRA2/Home
	//
	// NOTE: The injected beans can also be tied to a particular deployment. Note necessary here
	// but shown to see possibility.
	@Test
	@InSequence(2)
	@RunAsClient
	@OperateOnDeployment("login")
	public void shouldLoginSuccessfully(
			@Drone @OperateOnDeployment("login") WebDriver browser,
			@ArquillianResource @OperateOnDeployment("login") URL deploymentUrl)
			throws Exception {
		log.info("GAV::Deployment URL is::"
				+ deploymentUrl.toExternalForm() + "login.jsf");

		// Perform the get and render the content
		browser.get(deploymentUrl.toExternalForm() + "login.jsf"); // first page

		// Fire in the data
		userName.sendKeys("demo");
		password.sendKeys("demo123");

		//Will wait until full page is rendered by request guard or unless timeout occurs
		guardHttp(loginButton).click();
		assertEquals("Welcome", facesMessage.getText().trim());

		//Will wait until the Ajax request has finished or timed-out
		guardAjax(whoAmI).click(); 
		assertTrue(signedAs.getText().contains("demo"));
	}

	@Test
	@InSequence(3)
	@RunAsClient
	@OperateOnDeployment("login")
	public void shouldFailLogin(
			@Drone @OperateOnDeployment("login") WebDriver browser,
			@ArquillianResource @OperateOnDeployment("login") URL deploymentUrl)
			throws Exception {
		Logger log = Logger.getLogger(this.getClass().getName());
		log.fine("GAV::Deployment URL is::"
				+ deploymentUrl.toExternalForm() + "login.jsf");


		// Perform the get and render the content
		browser.get(deploymentUrl.toExternalForm() + "login.jsf"); // first page

		// Fire in the data
		userName.sendKeys("demo");
		password.sendKeys("demo124");

		//Will wait until full page is rendered by request guard or unless timeout occurs
		guardHttp(loginButton).click();
		assertNotEquals("Welcome", facesMessage.getText().trim());
	}

}
