import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.avaje.ebean.Ebean;
import models.Ausleihe;
import models.Benutzer;
import models.Item;
import models.Person;
import org.codehaus.jackson.JsonNode;
import org.junit.*;

import play.libs.Yaml;
import play.mvc.*;
import play.test.*;
import play.data.DynamicForm;
import play.data.validation.ValidationError;
import play.data.validation.Constraints.RequiredValidator;
import play.i18n.Lang;
import play.libs.F;
import play.libs.F.*;

import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;


/**
*
* Simple (JUnit) tests that can call all parts of a play app.
* If you are interested in mocking a whole application, see the wiki for more details.
*
*/
public class BenutzerTest {

    @Test
    public void Benutzerbeziehungen() {
        running(fakeApplication(inMemoryDatabase()), new Runnable() {
            public void run() {
                //Person checken
                Person p1 = Person.Create("Anna");
                assertThat(p1).isNotNull();
                //Datenbankinhalt prüfen
                Person p1db = Person.find.byId(p1.id);
                assertThat(p1db).isNotNull();
                assertThat(p1db.simpleName).isEqualTo("Anna");

                //Benutzer checken
                Benutzer b1 = new Benutzer();
                b1.vorname = "Sam";
                Benutzer.create(b1);
                assertThat(b1).isNotNull();
                assertThat(b1.vorname).isEqualTo("Sam");
                assertThat(b1.person).isNotNull();
                assertThat(b1.person.getName()).isEqualTo(b1.toString());

                //Datenbankinhalt prüfen
                Benutzer b1db = Benutzer.find.byId(b1.id);
                assertThat(b1db).isNotNull();
                assertThat(b1db.vorname).isEqualTo("Sam");
                assertThat(b1db.person).isNotNull();
                assertThat(b1db.person.getName()).isEqualTo(b1.toString());

            }
        });
    }
  
   
}
