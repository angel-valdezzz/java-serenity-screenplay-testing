package examples;
import java.util.UUID;
import net.serenitybdd.annotations.Managed;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import net.serenitybdd.screenplay.*;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.serenitybdd.screenplay.actions.*;
import net.serenitybdd.screenplay.questions.Text;
import net.serenitybdd.screenplay.waits.WaitUntil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;
import static net.serenitybdd.screenplay.GivenWhenThen.seeThat;
import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;
import static org.hamcrest.Matchers.equalTo;
@ExtendWith(SerenityJUnit5Extension.class)
class ParaBankUiTest {
  @Managed(driver="chrome") WebDriver driver;
  Actor customer; String username; final String password="DemoPass42!";
  @BeforeEach void setup() { customer=Actor.named("Customer").whoCan(BrowseTheWeb.with(driver)); username="qa"+UUID.randomUUID().toString().replace("-","").substring(0,18); }
  void home() { customer.attemptsTo(Open.url(System.getenv().getOrDefault("PARABANK_URL","https://parabank.parasoft.com/parabank")+"/index.htm")); }
  void text(String selector,String expected) { customer.attemptsTo(WaitUntil.the(selector,isVisible()).forNoMoreThan(20).seconds()); customer.should(seeThat(Text.of(selector),equalTo(expected))); }
  void form(boolean mismatch) { home(); customer.attemptsTo(Task.where("{0} completes a fictional customer registration",Click.on("a[href='register.htm']"),Enter.theValue("Automation").into("[id=\"customer.firstName\"]"),Enter.theValue("Example").into("[id=\"customer.lastName\"]"),Enter.theValue("123 Fictional Street").into("[id=\"customer.address.street\"]"),Enter.theValue("Demo City").into("[id=\"customer.address.city\"]"),Enter.theValue("CA").into("[id=\"customer.address.state\"]"),Enter.theValue("90210").into("[id=\"customer.address.zipCode\"]"),Enter.theValue("5551234567").into("[id=\"customer.phoneNumber\"]"),Enter.theValue("123456789").into("[id=\"customer.ssn\"]"),Enter.theValue(username).into("[id='customer.username']"),Enter.theValue(password).into("[id='customer.password']"),Enter.theValue(mismatch?"Different42!":password).into("#repeatedPassword"))); }
  void submit() { customer.attemptsTo(Click.on("#customerForm input[type=submit]")); }
  void register() { form(false); submit(); text("#rightPanel h1","Welcome "+username); }
  void login() { customer.attemptsTo(Task.where("{0} logs in",Enter.theValue(username).into("input[name=username]"),Enter.theValue(password).into("input[name=password]"),Click.on("#loginPanel input[type=submit]"))); }
  @Test void registration() { register(); }
  @Test void logoutLogin() { register(); customer.attemptsTo(Click.on("a[href='logout.htm']")); login(); text("#rightPanel h1","Accounts Overview"); }
  @Test void requiredFields() { home(); customer.attemptsTo(Click.on("a[href='register.htm']")); submit(); text("[id='customer.firstName.errors']","First name is required."); }
  @Test void passwordMismatch() { form(true); submit(); text("[id='repeatedPassword.errors']","Passwords did not match."); }
  @Test void unknownCredentials() { home(); login(); text("#rightPanel .error","The username and password could not be verified."); }
}
