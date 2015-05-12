package uk.gav.GavArqWeb;

import static org.jboss.arquillian.graphene.Graphene.guardHttp;
import static org.jboss.arquillian.graphene.Graphene.guardAjax;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.jboss.arquillian.graphene.findby.FindByJQuery;

@RunWith(Arquillian.class)
public class LoginScreenGrapheneTest {
	private static final String WEBAPP_SRC = "src/main/webapp";

	@Drone
	private WebDriver browser;

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

	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		return ShrinkWrap
				.create(WebArchive.class, "login.war")
				.addClasses(Credentials.class, User.class,
						LoginController.class)
				.addAsWebResource(new File(WEBAPP_SRC, "login.xhtml"))
				.addAsWebResource(new File(WEBAPP_SRC, "home.xhtml"))
				.addAsWebResource(new File(WEBAPP_SRC, "index.html"))
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
				.addAsWebInfResource(
						new StringAsset("<faces-config version=\"2.0\"/>"),
						"faces-config.xml");
	}

	@Test
	@RunAsClient
	public void should_login_successfully() {
		System.out.println("GAV::Deployment URL is::" + deploymentUrl.toExternalForm() + "login.jsf");
		
		browser.get(deploymentUrl.toExternalForm() + "login.jsf"); // first page
			
		userName.sendKeys("demo");
		password.sendKeys("demo");

		guardHttp(loginButton).click(); // 1. synchronize full-page request
		assertEquals("Welcome", facesMessage.getText().trim());

		guardAjax(whoAmI).click(); // 2. synchronize AJAX request
		assertTrue(signedAs.getText().contains("demo"));
	}
}
