package controllers;

import forms.BoardNoticeSearchForm;
import org.codehaus.jackson.JsonNode;
import org.joda.time.DateMidnight;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import play.Logger;
import play.data.DynamicForm;
import play.data.validation.ValidationError;
import play.libs.Json;
import play.mvc.*;
import models.*;
import play.data.Form;
import scala.Option;
import securesocial.core.java.SecureSocial;

import java.lang.NullPointerException;
import java.lang.RuntimeException;
import java.lang.String;
import java.lang.System;
import java.text.ParseException;
import java.util.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;

import views.html.board.*;
/**
 * @author Markus
 */


public class BoardNotices extends Controller{


    static Form<BoardNotice> newNoticeForm = Form.form(BoardNotice.class);
    static Form<BoardNoticeSearchForm> searchForm = Form.form(BoardNoticeSearchForm.class);



    @SecureSocial.UserAwareAction
    public static Result showNotices(NoticeTyp typ, int page) {
        Form<BoardNoticeSearchForm> bindForm = searchForm.bindFromRequest();

        if(bindForm.hasErrors() ){
            Logger.debug("erros: ");
            for(Map.Entry<String, List<ValidationError>> x:bindForm.errors().entrySet()){
                Logger.debug(x.getKey());
                for(ValidationError e:x.getValue()){
                    Logger.debug(e.message());
                }
            }
            BoardNotice.Page<BoardNotice> defaultResult = BoardNotice.search(typ, page);
            return badRequest(views.html.board.showNoticeBoard.render(defaultResult.values, typ, page,defaultResult.pages,defaultResult.rowcount,bindForm));
        }else{
            BoardNoticeSearchForm search = bindForm.get();

            Option<Boolean> post;
            if(search.acceptPost!=null){
                post = Option.<Boolean>apply(search.acceptPost);

            } else{
                post = Option.<Boolean>empty();
            }
            boolean friendsOnly=false;
            if(search.friendsOnly!= null){
                friendsOnly =search.friendsOnly;
            }

            Option<Double> area;
            if(search.area!=null && Benutzer.loggedInUser()!=null){
                if(Benutzer.loggedInUser().plz==null){
                    BoardNotice.Page<BoardNotice> defaultResult = BoardNotice.search(typ, page);
                    return badRequest(views.html.board.showNoticeBoard.render(defaultResult.values, typ, page,defaultResult.pages,defaultResult.rowcount,bindForm));
                }
                area = Option.<Double>apply(search.area);
            }else{
                area = Option.<Double>empty();
            }
            BoardNotice.Page<BoardNotice> result = BoardNotice.search(search.search,search.tags,typ, post,friendsOnly,area,page);
            return ok(views.html.board.showNoticeBoard.render(result.values, typ, page,result.pages,result.rowcount,bindForm));
        }
    }

    public static Result tagSearch(){

        StringBuilder s = new StringBuilder().append("[");
        List<Tag> tmp =Tag.find.setMaxRows(12).where().istartsWith("name",DynamicForm.form().bindFromRequest().get("name")).findList();
        if(tmp.isEmpty()) {
            return ok(Json.parse("[{\"label\":\"Kein Tag Gefunden\",\"valu\":-1}]"));
        } else{
            for(Tag t :tmp){
                s.append("{\"label\":\"").append(t.name);
                s.append("\",\"valu\":\"").append(t.name).append("\"},");
            }
            s.deleteCharAt(s.length()-1).append("]");
            JsonNode j = Json.parse(s.toString());


            return ok(j);
        }
    }
    public static Result tagFindeOrCreate(){
        String name=DynamicForm.form().bindFromRequest().get("name");
        StringBuilder s = new StringBuilder().append("[");
        List<Tag> tmp =Tag.find.setMaxRows(12).where().istartsWith("name",name).findList();

        for(Tag t :tmp){
            s.append("{\"label\":\"").append(t.name);
            s.append("\",\"valu\":\"").append(t.name);
            s.append("\",\"category\": \"").append("").append("\"},");
        }
        if(name.length()>=3){
            s.append("{\"label\": \"").append(name);
            s.append("\",\"valu\": \"").append(name);
            s.append("\",\"category\": \"").append("Neues Tag:").append("\"}").append("]");
        }else{
            s.deleteCharAt(s.length()-1).append("]");
        }
        JsonNode j = Json.parse(s.toString());


        return ok(j);
    }
    public static Result tagCreate(){
        String name=DynamicForm.form().bindFromRequest().get("name");
        Tag.create(name);

        return ok();
    }

    @SecureSocial.UserAwareAction
    public static Result showNotice(long id) {
        return ok(showBoardNotice.render(BoardNotice.find.byId(id), Benutzer.loggedInUser()));
    }

    @SecureSocial.SecuredAction
    public static Result anbieten() {
        return Results.ok(
                anbieten.render(newNoticeForm, "",NoticeTyp.Anbieten)
        );
    }
    @SecureSocial.SecuredAction
    public static Result anbietenVerarbeiteForm() {
        Form<BoardNotice> filledForm = newNoticeForm.bindFromRequest();

        // ---------- VALIDATION ------------

        //Titel nicht leer?
        if(filledForm.field("item.titel").valueOr("").isEmpty())
            filledForm.reject("item.titel", "Bitte wähle einen aussagekräftigen Namen für das Objekt");

        //Datum (bis) gewählt und in Zukunft?
        if(filledForm.field("bis").valueOr("").isEmpty()) {
            filledForm.reject("bis", "Bitte trage ein, wie lange du das Objekt anbieten möchtest!");
        } else {
            try {
                Date now = new Date();

                if(new SimpleDateFormat("yyyy-MM-dd").parse(filledForm.field("bis").value()).before(now))
                    filledForm.reject("bis", "Bitte wähle ein Datum in der Zukunft!");
            } catch (ParseException e ){
                filledForm.reject("bis", "Ungültiges Datum");
            }
        }

        //Mindestens eine Übergabemethode akzeptiert?
        if(!filledForm.hasErrors()) {
            BoardNotice data = filledForm.get();
            if(! (data.akzeptiertPost || data.akzeptiertUebergabe || data.akzeptiertSonstiges != "" )) {
                filledForm.reject("uebergabe","Bitte aktzeptiere mindestens eine Übergabemethode (egal welche).");
                return Results.badRequest(
                        anbieten.render(filledForm, "Bitte aktzeptiere mindestens eine Übergabemethode (egal welche).",NoticeTyp.Anbieten)
                );
            }
            if(data.akzeptiertUebergabe && Benutzer.loggedInUser().plz==null){
                filledForm.reject("akzeptiertUebergabe","Für die Distanzbegrenzung must du deine Plz im Profil angeben.");
                return Results.badRequest(
                        anbieten.render(filledForm, "Bitte korrigiere deine Eingabe.",NoticeTyp.Anbieten)
                );
            }
        }


        // ---------- VERARBEITUNG ------------

        if(filledForm.hasErrors()) {
            return Results.badRequest(
                    anbieten.render(filledForm, "Bitte korrigiere deine Eingabe.",NoticeTyp.Anbieten)
            );
        } else {
            BoardNotice data = filledForm.get();
            if (data.item.id == null){
                data.owner = Benutzer.loggedInUser(); //Verleiher definieren
                data.owner.person.items.add(data.item);
                data.owner.person.save();
            }else{
                data.item.update();
            }
            if (data.id==null){//Neu erstellen oder nur Updaten?
                data.owner = Benutzer.loggedInUser(); //Verleiher definieren
                data.typ = NoticeTyp.Anbieten;
                BoardNotice.create(data);
                Controller.flash("success", "Dein Angebot wurde  eingestellt.");
            }else{
                if(BoardNotice.find.byId(data.id).owner.equals(Benutzer.loggedInUser())){
                    data.update();
                    Controller.flash("success", "Dein Angebot wurde aktualisiert.");
                }
                Controller.flash("error", "Du kannst nur deine Eigenen Anzeigen Bearbeiten.");

            }

            return Results.redirect(routes.Profil.historie());
        }

    }
    @SecureSocial.SecuredAction
    public static Result suchen() {
        return Results.ok(
                anbieten.render(newNoticeForm, "",NoticeTyp.Suchen)
        );
    }

    @SecureSocial.SecuredAction
    public static Result bearbeiten(long id) {
        BoardNotice elem =  BoardNotice.find.byId(id);
        if(elem.owner.equals(Benutzer.loggedInUser())){
            if(elem.typ == NoticeTyp.Anbieten){
                return Results.ok(
                        anbieten.render(newNoticeForm.fill(elem), "",NoticeTyp.Anbieten)
                );
            } else{
                return Results.ok(
                        anbieten.render(newNoticeForm.fill(elem), "",NoticeTyp.Suchen)
                );
            }
        }else{
            Controller.flash("error", "Du kannst nur deine Eigenen Anzeigen Bearbeiten.");
            return redirect(routes.Profil.historie());
        }
    }

    @SecureSocial.SecuredAction
    public static Result suchenVerarbeiteForm() {
        Form<BoardNotice> filledForm = newNoticeForm.bindFromRequest();

        // ---------- VALIDATION ------------

        //Titel nicht leer?
        if(filledForm.field("item.titel").valueOr("").isEmpty())
            filledForm.reject("item.titel", "Bitte wähle einen aussagekräftigen Namen für das Objekt");

        //Datum (bis) gewählt und in Zukunft?
        if(filledForm.field("bis").valueOr("").isEmpty()) {
            filledForm.reject("bis", "Bitte trage ein, wie lange du das Objekt anbieten möchtest!");
        } else {
            try {
                Date now = new Date();

                if(new SimpleDateFormat("yyyy-MM-dd").parse(filledForm.field("bis").value()).before(now))
                    filledForm.reject("bis", "Bitte wähle ein Datum in der Zukunft!");
            } catch (ParseException e ){
                filledForm.reject("bis", "Ungültiges Datum");
            }
        }

        //Mindestens eine Übergabemethode akzeptiert?
        if(!filledForm.hasErrors()) {
            BoardNotice data = filledForm.get();
            if(! (data.akzeptiertPost || data.akzeptiertUebergabe || data.akzeptiertSonstiges != "" )) {
                filledForm.reject("uebergabe","Bitte aktzeptiere mindestens eine Übergabemethode (egal welche).");
                return Results.badRequest(
                        anbieten.render(filledForm, "Bitte aktzeptiere mindestens eine Übergabemethode (egal welche).",NoticeTyp.Suchen)
                );
            }
            if(data.akzeptiertUebergabe && Benutzer.loggedInUser().plz==null){
                filledForm.reject("akzeptiertUebergabe","Für die Distanzbegrenzung must du deine Plz im Profil angeben.");
                return Results.badRequest(
                        anbieten.render(filledForm, "Bitte korrigiere deine Eingabe.",NoticeTyp.Suchen)
                );
            }
        }


        // ---------- VERARBEITUNG ------------

        if(filledForm.hasErrors()) {
            return Results.badRequest(
                    anbieten.render(filledForm, "Bitte korrigiere deine Eingabe.",NoticeTyp.Suchen)
            );
        } else {
            BoardNotice data = filledForm.get();

            if (data.item.id == null){
                data.owner = Benutzer.loggedInUser(); //Verleiher definieren
                data.owner.person.items.add(data.item);
                data.owner.person.save();
            } else{
                data.item.update();
            }
            if (data.id==null){//Neu erstellen oder nur Updaten?
                data.owner = Benutzer.loggedInUser(); //Verleiher definieren
                data.typ = NoticeTyp.Suchen;
                BoardNotice.create(data);
                Controller.flash("success", "Deine Suchanzeige wurde eingestellt.");
            }else{
                if(BoardNotice.find.byId(data.id).owner.equals(Benutzer.loggedInUser())){
                    data.update();
                    Controller.flash("success", "Deine Suchanzeige wurde aktualisiert.");
                }
                Controller.flash("error", "Du kannst nur deine Eigenen Anzeigen Bearbeiten.");
            }

            return Results.redirect(routes.Profil.historie());
        }
    }

    @SecureSocial.SecuredAction
    public static Result archivieren(long id) {
        BoardNotice elem =  BoardNotice.find.ref(id);
        elem.archivieren();
        Controller.flash("success", "Die Anzeige wurde archiviert.");
        return Results.redirect(routes.Profil.historie());

    }


}


