import play.mvc.Http;
import play.mvc.Result;
import play.test.FakeRequest;

import java.util.HashMap;
import java.util.Map;

import static play.test.Helpers.POST;
import static play.test.Helpers.cookie;
import static play.test.Helpers.route;

public class TestHelper {
    // zum nutzen des Logins FakeRequest(...)..withCookies(login(...)) aufrufen
    public static Http.Cookie login(String email, String pw){
        Map<String,String> map = new HashMap<>();
        map.put("username",email);
        map.put("password",pw);
        Result login= route(new FakeRequest(POST,"/authenticate/userpass").withFormUrlEncodedBody(map));
        return cookie("id",login);
    }
}
