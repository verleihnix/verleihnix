package controllers;

import models.*;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import securesocial.core.java.SecureSocial;

import javax.validation.constraints.Null;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Christian
 * Date: 07.05.13
 * Time: 13:39
 * @author Christian, Christoph
 * Klasse, die aus dem Refactoring hervorgegangen ist.
 */
public class Profil {


    static Form<Benutzer> editForm = Form.form(Benutzer.class);

    @SecureSocial.SecuredAction
    public static Result submitProfileChange() {
        Form<Benutzer> filled = editForm.bindFromRequest();
        if (filled.hasErrors()) {
            Controller.flash("error", "Bitte korrigiere deine Eingaben.");
            //TODO: Validations schön machen, mit besseren messages und highlighting der falschen Eingabe (markus fragen wie er es gemacht hat
            return Results.redirect(routes.Profil.profilEdit());
        } else {
            Benutzer filledData =  filled.get();
            Benutzer loggedIn = Benutzer.loggedInUser();
            loggedIn.name = filledData.name;
            loggedIn.vorname = filledData.vorname;
            loggedIn.email = filledData.email;
            if(securesocial.core.AuthenticationMethod.UserPassword().equals(loggedIn.authMethod())){
                loggedIn.uId=loggedIn.email;
                Controller.flash("emailChanged", "Du hast deine Email geändert, bitte melde dich erneut an.");
            }
            loggedIn.geburtsdatum = filledData.geburtsdatum;
            loggedIn.wohnort = filledData.wohnort;
            //plz prüfung
            int results =PlzGeokoordinate.find.where().eq("plz",filledData.plz.plz).istartsWith("ort",filledData.wohnort).findRowCount();
            if(results ==0) {
                filled.reject("plz.plz","Unbekante Plz/Wohnort past nicht zu Plz");
                Controller.flash("error", "Bitte korrigiere deine Eingaben.");
                return Results.badRequest(views.html.profilEdit.render(loggedIn, filled));
            }
            if(results >1) {
                filled.reject("wohnort","Es gibt mehrer Orte mit dieser Plz, bitte gib den richtigen Ort an");
                Controller.flash("error", "Bitte korrigiere deine Eingaben.");
                return Results.badRequest(views.html.profilEdit.render(loggedIn, filled));
            }
            PlzGeokoordinate tmp =PlzGeokoordinate.find.where().eq("plz",filledData.plz.plz).istartsWith("ort",filledData.wohnort).findUnique();
            if(tmp!=null){
                loggedIn.plz=tmp;
                loggedIn.wohnort=tmp.ort;
            }
            loggedIn.update();
            Controller.flash("success", "Profil erfolgreich geändert!");
            return Results.redirect(routes.Profil.profil());
        }
    }

    @SecureSocial.SecuredAction
	public static Result profil() {
		Benutzer b = Benutzer.loggedInUser();

		return Results.ok(
                views.html.profil.render(b, b.person.verliehen, b.person.geliehen, Bewertung.average(b), true) // isLoggedInUser = true
        );
	}

    @SecureSocial.SecuredAction
    public static Result freundesListe() {
        Benutzer b = Benutzer.loggedInUser();

        return Results.ok(
                views.html.friendList.render(b.freundesListe,b.eigenePersonen)
                );
    }

    @SecureSocial.SecuredAction
    public static Result addFreund(long id) {
        Benutzer b = Benutzer.loggedInUser();
        Benutzer friend= Benutzer.find.byId(id);
        if(!b.equals(friend)){
            b.addFriend(friend);
            Controller.flash("success",friend.fullName() +" zur Freundesliste hinzugefügt");
        }
        return Results.seeOther(routes.Profil.freundesListe());
    }
    @SecureSocial.SecuredAction
    public static Result removeFreund(long id) {
        Benutzer b = Benutzer.loggedInUser();
        Benutzer friend= Benutzer.find.byId(id);
        b.removeFriend(friend);
        Controller.flash("success",friend.fullName() +" wurde von der Freundesliste entfernt");
        return Results.seeOther(routes.Profil.freundesListe());
    }

    @SecureSocial.UserAwareAction
    /** Fassung von Profil#profil, um fremde profile anzeigen lassen zu können.
     * Funktioniert auch, wenn man nicht eingeloggt ist. */
    public static Result fremdesProfil(Long id) {
        Benutzer b  = Benutzer.find.byId(id); // ACHTUNG, hier kann b == null sein!


        return Results.ok(
                views.html.profil.render(b, b.person.verliehen, b.person.geliehen, Bewertung.average(b), b.equals(Benutzer.loggedInUser()))
        );


    }

    @SecureSocial.SecuredAction
    public static Result profilEdit() {
        Benutzer user = Benutzer.loggedInUser();
        editForm =  editForm.fill(user);

        return Results.ok(views.html.profilEdit.render(user, editForm));
    }

    @SecureSocial.SecuredAction
     public static Result historie() {
        Benutzer b = Benutzer.loggedInUser();
        return Results.ok(
                views.html.historie.render(b.person.geliehen, b.person.verliehen, b.boardNotices)
        );
    }
}
