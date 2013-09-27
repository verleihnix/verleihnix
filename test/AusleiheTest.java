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
public class AusleiheTest {

    @Test
    public void ausleihe() {
        running(fakeApplication(inMemoryDatabase()), new Runnable() {
            public void run() {
                Person verleiher = Person.find.byId(100l);
                Person ausleiher = Person.find.byId(101l);

                assertThat(verleiher).isNotNull();
                assertThat(ausleiher).isNotNull();

                Ausleihe ausleihe = new Ausleihe();
                ausleihe.verleiher = verleiher;
                ausleihe.leiher = ausleiher;
                ausleihe.item = Item.find.byId(1000l);
                Ausleihe.create(ausleihe);

                // prüfen ob Ausleihe mit richtigen Daten in Datenbank
                Ausleihe dbAusleihe = Ausleihe.find.byId(ausleihe.id);
                assertThat(dbAusleihe).isNotNull();
                assertThat(dbAusleihe.verleiher).isEqualTo(verleiher);
                assertThat(dbAusleihe.leiher).isEqualTo(ausleiher);
                assertThat(dbAusleihe.item).isNotNull();
            }
        });
    }

}
