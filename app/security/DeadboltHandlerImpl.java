package security;
import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.*;

import controllers.routes;
import models.Benutzer;
import play.mvc.Http;
import play.mvc.Result;
import securesocial.core.Identity;
import securesocial.core.UserId;
import securesocial.core.java.SecureSocial;
import service.UserService;

import static play.mvc.Results.*;

public class DeadboltHandlerImpl implements DeadboltHandler{


    @Override
    public Result beforeAuthCheck(Http.Context context) {
        return null;
    }

    @Override
    public Subject getSubject(Http.Context context) {
        return ((Benutzer)context.args.get(SecureSocial.USER_KEY));
    }

    @Override
    public Result onAuthFailure(Http.Context context, String s) {
        return unauthorized("kein zugriff" + s);
    }

    @Override
    public DynamicResourceHandler getDynamicResourceHandler(Http.Context context) {
        {
            return null;
        }
    }
}
