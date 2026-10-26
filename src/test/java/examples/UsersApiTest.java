package examples;
import java.util.*;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.rest.abilities.CallAnApi;
import net.serenitybdd.screenplay.rest.interactions.*;
import net.serenitybdd.rest.SerenityRest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(SerenityJUnit5Extension.class)
class UsersApiTest {
  Actor operator; String key; List<String> ids=new ArrayList<>();
  @BeforeEach void setup() { key=Objects.requireNonNull(System.getenv("DEMO_API_KEY")); operator=Actor.named("API operator").whoCan(CallAnApi.at(Objects.requireNonNull(System.getenv("DEMO_BASE_URL")))); }
  @AfterEach void cleanup() { for(String id:ids) { operator.attemptsTo(Delete.from("/users/"+id).with(r->r.header("X-API-Key",key))); assertTrue(List.of(204,404).contains(SerenityRest.lastResponse().statusCode())); } }
  String create(String email) { operator.attemptsTo(Post.to("/users").with(r->r.header("X-API-Key",key).contentType("application/json").body(Map.of("name","Screenplay","email",email,"role","sales")))); assertEquals(201,SerenityRest.lastResponse().statusCode()); String id=SerenityRest.lastResponse().jsonPath().getString("id"); ids.add(id); assertEquals(email,SerenityRest.lastResponse().jsonPath().getString("email")); return id; }
  @Test void createRetrieveAndDeactivate() {
    String id=create(UUID.randomUUID()+"@example.com");
    operator.attemptsTo(Get.resource("/users/"+id).with(r->r.header("X-API-Key",key))); assertEquals(200,SerenityRest.lastResponse().statusCode()); assertEquals(id,SerenityRest.lastResponse().jsonPath().getString("id"));
    operator.attemptsTo(Post.to("/users/"+id+"/deactivate").with(r->r.header("X-API-Key",key))); assertEquals(200,SerenityRest.lastResponse().statusCode()); assertFalse(SerenityRest.lastResponse().jsonPath().getBoolean("active"));
  }
  @Test void duplicateEmail() { String email=UUID.randomUUID()+"@example.com"; create(email); operator.attemptsTo(Post.to("/users").with(r->r.header("X-API-Key",key).contentType("application/json").body(Map.of("name","Duplicate","email",email)))); assertEquals(409,SerenityRest.lastResponse().statusCode()); }
  @Test void invalidCredential() { operator.attemptsTo(Get.resource("/users").with(r->r.header("X-API-Key","invalid"))); assertEquals(401,SerenityRest.lastResponse().statusCode()); }
}
