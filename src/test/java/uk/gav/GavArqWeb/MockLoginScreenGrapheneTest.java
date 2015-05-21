package uk.gav.GavArqWeb;

import static org.jboss.arquillian.graphene.Graphene.guardAjax;
import static org.jboss.arquillian.graphene.Graphene.guardHttp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.logging.Logger;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
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
 *         As this is a mock test, the MockLoginBean specialisation will be used in lieu of the true
 *         LoginBean and, as a result, bypass true persistence.
 * 
 */
@RunWith(Arquillian.class)
public class MockLoginScreenGrapheneTest {

	// Use standard Java logging. Note that as this bean is hosted in container, it it the container
	// logging.properties that will be used rather than that defined by this project.
	private static Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

	private static final String WEBAPP_SRC = "src/main/webapp";
	
	// Injected by Arquillian as the landing context for the application
	@ArquillianResource
	private URL deploymentUrl;

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

	// This deployment will obviously run in the container
	@Deployment(name = "login")
	public static Archive<WebArchive> createWARDeployment() {
		// Create the web archive to test. 
		// NOTE: The MockLoginBean and the LoginBean; the latter is still
		// required as MockLoginBean extends it as a specialisation.
		WebArchive war = ShrinkWrap
				.create(WebArchive.class, "login.war")
				.addClasses(Credentials.class,
							User.class, 
							LoginController.class,
							LoginBean.class,  //Keep in here to ensure overridden
							MockLoginBean.class)
				.addAsWebResource(new File(WEBAPP_SRC, "login.xhtml"))
				.addAsWebResource(new File(WEBAPP_SRC, "home.xhtml"))
				.addAsWebResource(new File(WEBAPP_SRC, "index.html"))
				// Need the file, but it's empty
				.addAsResource("Mocktesting-persistence.xml",
						"META-INF/persistence.xml")
				// Defines the specialisation alternative
				.addAsWebInfResource("test-beans.xml", "beans.xml")
				.addAsWebInfResource(
						new StringAsset("<faces-config version=\"2.0\"/>"),
						"faces-config.xml");

		log.fine("Content of WAR is::" + war.toString(true));
		return war;
	}
	
	// Inject the WebDriver to allow interactions with the desired browser (as specified in
	// arquillian.xml. @Drone manages the Webdriver within the Arquillian framework and is 
	// explained in details at: https://docs.jboss.org/author/display/ARQ/Drone
	// It also utilises Arquillian Graphene to manage the the injection of pages, AJAX etc.
	// The Graphene 2 project is designed as set of extensions for Selenium WebDriver project focused 
	// on rapid development and usability in Java environment
	// See https://docs.jboss.org/author/display/ARQGRA2/Home
	@Test
	@RunAsClient
	public void should_login_successfully(@Drone WebDriver browser) throws Exception {
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

}
