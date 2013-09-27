package controllers;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import forms.BewertungForm;
import models.Ausleihe;
import models.Benutzer;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import securesocial.core.java.SecureSocial;

/**
 * Created with IntelliJ IDEA.
 * User: Christian
 * Date: 07.05.13
 * Time: 13:54
 * @author Christian,Christoph
 * klasse, die aus dem refactoring entstanden ist.
 */
public class Verleih {


    static Form<Ausleihe> ausleiheForm = Form.form(Ausleihe.class);

    //bsp wie man auf user Rolle prüft
    //@SecureSocial.SecuredAction
    //@Restrict(value=@Group("user"),content = "user",deferred=true)
    @SecureSocial.SecuredAction
    public static Result ausleihen() {
        return Results.ok(
                views.html.ausleihen.render(ausleiheForm, "", Benutzer.loggedInUser().knownContacts())
        );
    }

    @SecureSocial.SecuredAction
    public static Result verleihen() {
        return Results.ok(
                views.html.verleihen.render(ausleiheForm, "", Benutzer.loggedInUser().knownContacts())
        );
    }

    @SecureSocial.SecuredAction
    public static Result neueAusleihe() {

        Form<Ausleihe> filledForm = ausleiheForm.bindFromRequest();

        // ---------- VALIDATION ------------

        if(filledForm.field("verleiher.id").valueOr("").isEmpty()){
            //Eine neue Person wurde eingetragen
            if(filledForm.field("verleiher.simpleName").valueOr("").isEmpty())
                filledForm.reject("verleiher.simpleName",  "Bitte trage den Namen der Person ein, von der du das Objekt auswählst.");
        }

        // ---------- VERARBEITUNG ------------

        if(filledForm.hasErrors()) {
            return Results.badRequest(
                    views.html.ausleihen.render(filledForm, "Bitte korrigiere deine Eingabe.", Benutzer.loggedInUser().knownContacts())
            );
        } else {
            Ausleihe data = filledForm.get();

            if (data.verleiher.id == null || data.verleiher.id == 0)
                data.verleiher.save();
            data.leiher = Benutzer.loggedInUser().person; //leiher definieren
            Ausleihe.create(data);
            Controller.flash("success", "Der Verleihvorgang wurde hinzugefügt.");
            return Results.redirect(routes.Profil.historie());
        }
    }

    @SecureSocial.SecuredAction
    public static Result neueVerleihe() {

        Form<Ausleihe> filledForm = ausleiheForm.bindFromRequest();
        if(filledForm.hasErrors()) {
            return Results.badRequest(
                    views.html.verleihen.render(filledForm, "Bitte korrigiere deine Eingabe.", Benutzer.loggedInUser().knownContacts())
                    //Das war nix :(" + "\n Vorhandene Fehler: " + filledForm.errors().toString()
            );
        } else {
            Ausleihe data = filledForm.get();

            if(data.leiher.id == null && data.leiher.getName() == ""){
                return Results.badRequest(views.html.verleihen.render(filledForm, "Bitte trage mindestens den Namen der neuen Benutzer ein.", Benutzer.loggedInUser().knownContacts()));
            } else if(data.leiher.id != null && data.leiher.id == -1) {
                return Results.badRequest(views.html.verleihen.render(filledForm, "Bitte wähle die Benutzer, an die du verleihen möchtest.", Benutzer.loggedInUser().knownContacts()));
            } else {
                if (data.leiher.id == null || data.leiher.id == 0)
                    data.leiher.save();
                data.verleiher = Benutzer.loggedInUser().person; //Verleiher definieren
                Ausleihe.create(data);
                Controller.flash("success", "Der Verleihvorgang wurde hinzugefügt.");
                return Results.redirect(routes.Profil.historie());
            }
        }
    }

    @SecureSocial.SecuredAction
    public static Result rueckgabe(long id) {
        Ausleihe.rueckgeben(id);
        Controller.flash("success", "Das Objekt wurde zurückgegeben.");
        return Results.redirect(routes.Profil.historie());
    }

    @SecureSocial.SecuredAction
    public static Result bewerten(){   //TODO speicherung mit kurzer flash msg bestätigen,für weitere editirung speeren
        Form<BewertungForm> bewertung =Form.form(BewertungForm.class).bindFromRequest();
        if(bewertung.hasErrors()){
            return Results.badRequest();
        }else{
            Logger.debug("succ");
            BewertungForm b = bewertung.get();
            Ausleihe ausleihe= Ausleihe.find.byId(b.ausleihe.id);

            ausleihe.bewerten(b.wertung);
            return Results.ok("#" + b.ausleihe.id);
        }


    }
}
