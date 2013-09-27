import controllers.routes;
import models.*;
import org.junit.Test;
import play.Logger;
import play.mvc.Http;
import play.mvc.Result;
import play.test.FakeRequest;

import java.util.HashMap;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;


/**
*
* Simple (JUnit) tests that can call all parts of a play app.
* If you are interested in mocking a whole application, see the wiki for more details.
*
*/
public class MessagingTest {



    
    /*@Test
    public void renderTemplate() {
        Content html = views.html.index.render("Your new application is ready.");
        assertThat(contentType(html)).isEqualTo("text/html");
        assertThat(contentAsString(html)).contains("Your new application is ready.");
    }*/


    @Test
    public void SendMessage() {
        running(fakeApplication(inMemoryDatabase()), new Runnable() {
            public void run() {
                Benutzer sender = Benutzer.find.byId(500l);
                Benutzer empfaenger = Benutzer.find.byId(501l);

                assertThat(sender).isNotNull();
                assertThat(empfaenger).isNotNull();



                Http.Cookie login =TestHelper.login(sender.email, "123456");


                Map<String,String> map = new HashMap<>();
                map.put("empfaenger[0].id","501");
                map.put("subject","Test Message");
                map.put("message","a test message");

                FakeRequest sendMessage = new FakeRequest(POST,"/msg/write").withFormUrlEncodedBody(map).withCookies(login);
                Result result = route(sendMessage);
                assertThat(status(result)).isEqualTo(SEE_OTHER);

                assertThat(flash(result).get("Success")).contains("Nachricht Erfolgreich versendet");


                // prüfen ob Message richtig gespeichert wurde
                Message dbmessage = Message.find.where().eq("subject","Test Message").findUnique();
                assertThat(dbmessage).isNotNull();
                assertThat(dbmessage.sender).isEqualTo(sender);
                assertThat(dbmessage.empfaenger).isNotEmpty();
                //assertThat(dbmessage.message).isEqualTo("a test message");
                for(EmpfangeneMessage em:dbmessage.empfaenger){
                    assertThat(em.empfaenger).isEqualTo(empfaenger);
                }


                Logger.debug("Senden Fehlerfrei");


            }
        });
    }

    @Test
    public void SendMessageWithoutEmpf() {
        running(fakeApplication(inMemoryDatabase()), new Runnable() {
            public void run() {
                Benutzer sender = Benutzer.find.byId(500l);

                assertThat(sender).isNotNull();
                Http.Cookie login =TestHelper.login(sender.email, "123456");


                Map<String,String> map = new HashMap<>();
                //map.put("empfaenger[0].id","501");
                map.put("subject","Test Message");
                map.put("message","a test message");

                FakeRequest sendMessage = new FakeRequest(POST,"/msg/write").withFormUrlEncodedBody(map).withCookies(login);
                Result result = route(sendMessage);
                assertThat(status(result)).isEqualTo(BAD_REQUEST);

                assertThat(flash(result).get("empfaenger")).contains("Mindestens ein Empfänger");

                // sicherstellen das msg nicht gespeichert wurde
                assertThat(Message.find.all().size()).isEqualTo(0);

            }
        });
    }
    @Test
    public void SendMessageWithoutSubject() {
        running(fakeApplication(inMemoryDatabase()), new Runnable() {
            public void run() {
                Benutzer sender = Benutzer.find.byId(500l);

                assertThat(sender).isNotNull();
                Http.Cookie login =TestHelper.login(sender.email, "123456");


                Map<String,String> map = new HashMap<>();
                map.put("empfaenger[0].id","501");
                //map.put("subject","Test Message");
                map.put("message","a test message");

                FakeRequest sendMessage = new FakeRequest(POST,"/msg/write").withFormUrlEncodedBody(map).withCookies(login);
                Result result = route(sendMessage);
                assertThat(status(result)).isEqualTo(BAD_REQUEST);

                assertThat(flash(result).get("subject")).contains("Mindestens 3 Zeichen");

                // sicherstellen das msg nicht gespeichert wurde
                assertThat(Message.find.all().size()).isEqualTo(0);

            }
        });
    }
    @Test
    public void SendMessageWithoutBody() {
        running(fakeApplication(inMemoryDatabase()), new Runnable() {
            public void run() {
                Benutzer sender = Benutzer.find.byId(500l);

                assertThat(sender).isNotNull();
                Http.Cookie login =TestHelper.login(sender.email, "123456");


                Map<String,String> map = new HashMap<>();
                map.put("empfaenger[0].id","501");
                map.put("subject","Test Message");
                //map.put("message","a test message");

                FakeRequest sendMessage = new FakeRequest(POST,"/msg/write").withFormUrlEncodedBody(map).withCookies(login);
                Result result = route(sendMessage);
                assertThat(status(result)).isEqualTo(BAD_REQUEST);

                assertThat(flash(result).get("message")).contains("Mindestens 3 Zeichen");

                // sicherstellen das msg nicht gespeichert wurde
                assertThat(Message.find.all().size()).isEqualTo(0);

            }
        });
    }

    @Test
    public void ReadDeleteMessage() {
        running(fakeApplication(inMemoryDatabase()), new Runnable() {
            public void run() {
                Benutzer sender = Benutzer.find.byId(500l);
                Benutzer empfaenger = Benutzer.find.byId(501l);

                assertThat(sender).isNotNull();
                assertThat(empfaenger).isNotNull();



                Http.Cookie login =TestHelper.login(sender.email,"123456");


                Map<String,String> map = new HashMap<>();
                map.put("empfaenger[0].id","501");
                map.put("subject","Test Message");
                map.put("message","a test message");

                FakeRequest sendMessage = new FakeRequest(POST,"/msg/write").withFormUrlEncodedBody(map).withCookies(login);
                Result resultSend = route(sendMessage);
                assertThat(status(resultSend)).isEqualTo(SEE_OTHER);

                assertThat(flash(resultSend).get("Success")).contains("Nachricht Erfolgreich versendet");


                // prüfen ob Message richtig gespeichert wurde
                Message dbmessage = Message.find.where().eq("subject","Test Message").findUnique();
                assertThat(dbmessage).isNotNull();
                assertThat(dbmessage.sender).isEqualTo(sender);
                //assertThat(dbmessage.message).isEqualTo("a test message");
                assertThat(dbmessage.empfaenger).isNotEmpty();
                assertThat(dbmessage.referred).isEqualTo(2);
                for(EmpfangeneMessage em:dbmessage.empfaenger){
                    assertThat(em.empfaenger).isEqualTo(empfaenger);
                    //read
                    assertThat(em.read).isFalse();
                    assertThat(em.deleted).isFalse();
                    Http.Cookie login2 =TestHelper.login(empfaenger.email,"123456");
                    FakeRequest readMessage = new FakeRequest(GET,"/msg/read/"+em.id).withCookies(login2);
                    Result resultRead = route(readMessage);
                    assertThat(status(resultRead)).isEqualTo(OK);
                    em.refresh();
                    assertThat(em.read).isTrue();
                    assertThat(em.deleted).isFalse();
                    dbmessage.refresh();
                    assertThat(dbmessage.referred).isEqualTo(2);

                    //delete
                    FakeRequest deleteMessage = new FakeRequest(GET,"/msg/delete/"+em.id).withCookies(login2);
                    Result resultDelete = route(deleteMessage);
                    assertThat(status(resultDelete)).isEqualTo(SEE_OTHER);
                    em.refresh();
                    assertThat(em.deleted).isTrue();
                    dbmessage.refresh();
                    assertThat(dbmessage.referred).isEqualTo(1);

                    //read after delete
                    Result resultRead2 = route(sendMessage);
                    assertThat(status(resultRead2)).isEqualTo(SEE_OTHER);
                }
                FakeRequest deleteSendMessage = new FakeRequest(GET,"/msg/deleteSend/"+dbmessage.id).withCookies(login);
                Result resultDeleteSend = route(deleteSendMessage);
                assertThat(status(resultDeleteSend)).isEqualTo(SEE_OTHER);
                assertThat(Message.find.byId(dbmessage.id)).isNull();
            }
        });
    }

    @Test
    public void ownerCheck() {
        running(fakeApplication(inMemoryDatabase()), new Runnable() {
            public void run() {
                Benutzer sender = Benutzer.find.byId(500l);
                Benutzer user = Benutzer.find.byId(501l);

                assertThat(sender).isNotNull();
                assertThat(user).isNotNull();



                Http.Cookie login =TestHelper.login(sender.email,"123456");
                Http.Cookie login2 =TestHelper.login(user.email,"123456");

                Map<String,String> map = new HashMap<>();
                map.put("empfaenger[0].id","500");
                map.put("subject","Test Message");
                map.put("message","a test message");

                FakeRequest sendMessage = new FakeRequest(POST,"/msg/write").withFormUrlEncodedBody(map).withCookies(login);
                Result resultSend = route(sendMessage);
                assertThat(status(resultSend)).isEqualTo(SEE_OTHER);

                assertThat(flash(resultSend).get("Success")).contains("Nachricht Erfolgreich versendet");

                Message dbmessage = Message.find.where().eq("subject","Test Message").findUnique();
                assertThat(dbmessage).isNotNull();
                assertThat(dbmessage.sender).isEqualTo(sender);
                assertThat(dbmessage.empfaenger).isNotEmpty();
                assertThat(dbmessage.referred).isEqualTo(2);
                for(EmpfangeneMessage em:dbmessage.empfaenger){
                    assertThat(em.empfaenger).isEqualTo(sender);
                    //try read
                    assertThat(em.read).isFalse();
                    assertThat(em.deleted).isFalse();

                    FakeRequest readMessage = new FakeRequest(GET,"/msg/read/"+em.id).withCookies(login2);
                    Result resultRead = route(readMessage);
                    assertThat(status(resultRead)).isEqualTo(SEE_OTHER);
                    em.refresh();
                    assertThat(em.read).isFalse();
                    assertThat(em.deleted).isFalse();
                    dbmessage.refresh();
                    assertThat(dbmessage.referred).isEqualTo(2);

                    //try delete
                    FakeRequest deleteMessage = new FakeRequest(GET,"/msg/delete/"+em.id).withCookies(login2);
                    Result resultDelete = route(deleteMessage);
                    assertThat(status(resultDelete)).isEqualTo(SEE_OTHER);
                    em.refresh();
                    assertThat(em.deleted).isFalse();
                    dbmessage.refresh();
                    assertThat(dbmessage.referred).isEqualTo(2);
                    assertThat(Message.find.byId(dbmessage.id)).isNotNull();
                }
                FakeRequest deleteSendMessage = new FakeRequest(GET,"/msg/deleteSend/"+dbmessage.id).withCookies(login2);
                Result resultDeleteSend = route(deleteSendMessage);
                assertThat(status(resultDeleteSend)).isEqualTo(SEE_OTHER);
                assertThat(Message.find.byId(dbmessage.id)).isNotNull();
                dbmessage.refresh();
                assertThat(dbmessage.referred).isEqualTo(2);

            }
        });
    }
   
}
