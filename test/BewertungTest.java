import models.*;
import org.junit.Test;
import play.Logger;
import play.mvc.Http;
import play.mvc.Result;
import play.test.FakeRequest;

import java.util.HashMap;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.SEE_OTHER;
import static play.test.Helpers.*;

/**
 * Created with IntelliJ IDEA.
 * User: Christian
 * Date: 23.05.13
 * Time: 10:49
 * To change this template use File | Settings | File Templates.
 */
public class BewertungTest {


    @Test
    public void einzelneBewertungTest() {
        running(fakeApplication(inMemoryDatabase()), new Runnable() {
            public void run() {

                HashMap<String,String> m = new HashMap<>();
                m.put("ausleihe.id", "2000");
                m.put("wertung", "3");   // wir bewerten user 501




                Http.Cookie login =TestHelper.login("test1@sample.com", "123456");

                FakeRequest bewerte = new FakeRequest(POST,"/bewerten").withFormUrlEncodedBody(m).withCookies(login);
                Result result = route(bewerte);

                assertThat(status(result)).isEqualTo(OK); // Prüfen, ob HTTP-Request erfolgreich bearbeitet wurde

                Bewertung actual = Bewertung.getBewertungFor(Ausleihe.find.byId(2000l),Benutzer.find.byId(501l));

                assertThat(actual.bewerter.id).isEqualTo(Benutzer.find.byId(500l).id);
                //testen darauf, ob der bewerter richtg ist -> äquiv. zu Benutzer.loggedInUser();
                assertThat(actual.wertung).isEqualTo(3);
                // testen darauf, ob die oben eingegebene zahl auch wirklich in der db angekommen ist
            }
        });



    }

    @Test
    public void testAverage() {
          throw new UnsupportedOperationException("Nicht implementiert, TODO: Implementieren");

    }


}
