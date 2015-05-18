package uk.gav.GavArqWeb;

import static org.jboss.arquillian.graphene.Graphene.guardAjax;
import static org.jboss.arquillian.graphene.Graphene.guardHttp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

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
 *         'browser' will be injected with the appropriate browser
 *         implementation taken from arquillian.xml config 'deploymentUrl' will
 *         be injected with the initial URL context The 'FindBy' annotations
 *         will inject the corresponding variables with the corresponding
 *         elements from the html rendered
 * 
 */
@RunWith(Arquillian.class)
public class LoginScreenGrapheneTest {
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
		// Create the web archive to test
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
				.addAsWebInfResource(
						new StringAsset("<faces-config version=\"2.0\"/>"),
						"faces-config.xml");

		System.out.println("WAR Content is::" + war.toString(true));
		return war;
	}

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

		System.out.println("JAR Content is::" + jar.toString(true));
		return jar;
	}

	// Note frig as 'UsingDataSet' doesn't work for 'RunAsClient'
	// Even the 'frig' doesn't work because this test running in the server
	// for some reason needs the WebDriver class still injecting.
	@Test
	@InSequence(1)
	@UsingDataSet("credentials.yml")
	@OperateOnDeployment("setup")
	@Cleanup(phase = TestExecutionPhase.NONE) //Could go on class as well
	public void populateDatabaseWorkaround() throws Exception {
		// Populates database since unable to use @UsingDataSet with
		// @RunAsClient
		// Check https://issues.jboss.org/browse/ARQ-1077
	}

	@Test
	@InSequence(2)
	@RunAsClient
	@OperateOnDeployment("login")
	public void shouldLoginSuccessfully(
			@Drone @OperateOnDeployment("login") WebDriver browser,
			@ArquillianResource @OperateOnDeployment("login") URL deploymentUrl)
			throws Exception {
		System.out.println("GAV::Deployment URL is::"
				+ deploymentUrl.toExternalForm() + "login.jsf");

		// Perform the get and render the content
		browser.get(deploymentUrl.toExternalForm() + "login.jsf"); // first page

		// Fire in the data
		userName.sendKeys("demo");
		password.sendKeys("demo123");

		guardHttp(loginButton).click(); // 1. synchronize full-page request
		assertEquals("Welcome", facesMessage.getText().trim());

		guardAjax(whoAmI).click(); // 2. synchronize AJAX request
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
		System.out.println("GAV::Deployment URL is::"
				+ deploymentUrl.toExternalForm() + "login.jsf");

		// Perform the get and render the content
		browser.get(deploymentUrl.toExternalForm() + "login.jsf"); // first page

		// Fire in the data
		userName.sendKeys("demo");
		password.sendKeys("demo124");

		guardHttp(loginButton).click(); // 1. synchronize full-page request
		assertNotEquals("Welcome", facesMessage.getText().trim());
	}

}
