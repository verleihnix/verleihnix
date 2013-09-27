import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.avaje.ebean.Ebean;
import models.*;
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
public class BoardNoticeTest {

    @Test
    public void anbieten() {
        running(fakeApplication(inMemoryDatabase()), new Runnable() {
            public void run() {
                Benutzer owner = Benutzer.find.byId(500L);
                Item item = Item.find.byId(1001L);

                assertThat(owner).isNotNull();
                assertThat(item).isNotNull();

                BoardNotice notice = new BoardNotice();
                notice.owner = owner;
                notice.item = item;
                notice.typ = NoticeTyp.Suchen;
                BoardNotice.create(notice);

                // prüfen ob Ausleihe mit richtigen Daten in Datenbank
                BoardNotice dbNotice = BoardNotice.find.byId(notice.id);
                assertThat(dbNotice).isNotNull();
                assertThat(dbNotice.owner).isNotNull();
                assertThat(dbNotice.owner).isEqualTo(owner);
                assertThat(dbNotice.item).isNotNull();
                assertThat(dbNotice.item).isEqualTo(item);

                //Interessenten prüfen
                Benutzer interessent = Benutzer.find.byId(501L);
                assertThat(interessent).isNotNull();

                dbNotice.AddInteressent(interessent);
                assertThat(dbNotice.interessenten.contains(interessent)).isTrue();
                assertThat(interessent.interessiertAn.contains(dbNotice)).isTrue();

                dbNotice.RemoveInteressent(interessent);
                assertThat(dbNotice.interessenten.contains(interessent)).isFalse();
                assertThat(interessent.interessiertAn.contains(dbNotice)).isFalse();

            }
        });
    }

}
