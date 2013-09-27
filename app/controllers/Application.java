package controllers;

import forms.BewertungForm;
import play.Routes;
import play.mvc.*;
import play.data.*;
import models.*;
import play.Logger;

import static securesocial.core.java.SecureSocial.*;
import be.objectify.deadbolt.java.actions.*;

@UserAwareAction
@DeferredDeadbolt
public class Application extends Controller {

    public static Result index() {
        return BoardNotices.showNotices(NoticeTyp.Anbieten, 1);
        //return ok(index.render("Our new application is ready!"));
    }

    public static Result datenbankDiag(){
        return ok(
                views.html.datenbankDiag.render(Ausleihe.all(),Item.all(),Person.find.all())
        );
    }

    /**
     * Erzeugt dynamich routen die mit javascript aufgerufen werden können
     * in js zb: jsRoutes.controllers.BoardNotices.showNotice(num).url
     * @return
     */
    public static Result javascriptRoutes(){
        response().setContentType("text/javascript");
        return ok(
                Routes.javascriptRouter(
                        "jsRoutes",
                        controllers.routes.javascript.BoardNotices.showNotice(),
                        controllers.routes.javascript.Profil.fremdesProfil()//...
                )
        );
    }


}
