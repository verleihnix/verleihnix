package controllers;


import forms.GetMessageForm;
import forms.IdListForm;
import forms.WriteMessageForm;
import org.codehaus.jackson.JsonNode;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.*;
import models.*;

import static securesocial.core.java.SecureSocial.*;
import be.objectify.deadbolt.java.actions.*;
import scala.Option;
import views.html.msg.showMessages;
import views.html.msg.writeMsg;

import java.util.*;


@SecuredAction
@DeferredDeadbolt
public class Messaging extends Controller{

    static Form<IdListForm> messageDeleteForm = Form.form(IdListForm.class);
    static Form<WriteMessageForm> sendMessageForm = Form.form(WriteMessageForm.class);

    public static Result showMessages(int page) {
        Option<String> read= ctx()._requestHeader().getQueryString("read");
        List<EmpfangeneMessage> rmsg;
        if(read.isDefined() && Boolean.valueOf(read.get())){
            rmsg = EmpfangeneMessage.getNewMessages(page);
        }  else {
            rmsg = EmpfangeneMessage.getMessages(page);
        }
        return ok(showMessages.render(rmsg,EmpfangeneMessage.getPageCount(),page));
    }
    public static Result readMessage(long id) {
        Message msg= EmpfangeneMessage.readMessage(id);
        if(msg!=null){
            return  ok(views.html.msg.showMessage.render(msg));
        }else{
            flash("msg","Nachricht nicht gefunden");
            return redirect(controllers.routes.Messaging.showMessages(0));
        }
    }
    public static Result showSendMessages(int page) {

        return ok(views.html.msg.showSendMessages.render(Message.getSendMessages(page),Message.getPageCount(),page));
    }

    public static Result readSendMessage(long id) {
        Message msg= Message.readMessage(id);
        if(msg!=null){
            return  ok(views.html.msg.showSendMessage.render(msg));
        }else{
            flash("msg","Nachricht nicht gefunden");
            return redirect(controllers.routes.Messaging.showSendMessages(0));
        }
    }
    public static Result writeMessage() {

        Set<Benutzer> empfanger= new HashSet<>();
        Form<WriteMessageForm> form= Form.form(WriteMessageForm.class);
        Form<GetMessageForm> reply =Form.form(GetMessageForm.class).bindFromRequest();
        if(!reply.hasErrors()){
            Message oldMessage =reply.get().msg;
            if((oldMessage =Message.find.byId(oldMessage.id)) !=null ){

                empfanger= Benutzer.getEmpanger(oldMessage);
                if(!empfanger.isEmpty()){
                    form= form.fill(WriteMessageForm.reply(oldMessage,empfanger));
                }
            }

        }
        return ok(writeMsg.render(empfanger,form));
    }
    public static Result writeMessageTo(long empfId) {

        Set<Benutzer> empfanger= new HashSet<>();
        Form<WriteMessageForm> form= Form.form(WriteMessageForm.class);
        empfanger.add(Benutzer.find.byId(empfId));

        return ok(writeMsg.render(empfanger,form));
    }

    public static Result writeMessageAusleihe(Long ausleiheId,Long empfId){
        Ausleihe a = Ausleihe.find.byId(ausleiheId);

        Set<Benutzer> empfanger= new HashSet<>();
        empfanger.add(Benutzer.find.byId(empfId));
        Form<WriteMessageForm> form= Form.form(WriteMessageForm.class).fill(WriteMessageForm.zuAusleihe(a,Benutzer.find.byId(empfId)));

        return ok(writeMsg.render(empfanger,form));
    }

    public static Result writeMessageNotice(Long noticeId,Long empfId){
        BoardNotice b = BoardNotice.find.byId(noticeId);

        Set<Benutzer> empfanger= new HashSet<>();
        empfanger.add(Benutzer.find.byId(empfId));
        Form<WriteMessageForm> form= Form.form(WriteMessageForm.class).fill(WriteMessageForm.zuBoardNotice(b,Benutzer.find.byId(empfId)));

        return ok(writeMsg.render(empfanger,form));
    }

    public static Result sendMessages() {
        Form<WriteMessageForm> newMessageForm = sendMessageForm.bindFromRequest();
        Set<Benutzer> empf = new HashSet<>();
        if(newMessageForm.hasErrors()){
            if(newMessageForm.error("message")!=null){
                flash("message","Mindestens 3 Zeichen");
            }
            if(newMessageForm.error("subject")!=null){
                flash("subject","Mindestens 3 Zeichen");
            }
            if(newMessageForm.error("empfaenger")!=null){
                flash("empfaenger","Mindestens ein Empfänger");
            }else{
                for(Map.Entry<String,String> kv:newMessageForm.data().entrySet()){
                    if(kv.getKey().startsWith("empfaenger[")){
                        try{
                            Benutzer b=Benutzer.find.byId(Long.valueOf(kv.getValue()));
                            if(b !=null)  empf.add(b);
                        }catch (NumberFormatException ignore){}

                    }
                }
            }


            return badRequest(writeMsg.render(empf,newMessageForm));
        }   else{
            WriteMessageForm newMsg =newMessageForm.get();
            Message.send(newMsg);
            flash("Success","Nachricht Erfolgreich versendet");
            return seeOther(routes.Messaging.showSendMessages(0));
        }
    }

    public static Result deleteMessages() {
        Form<IdListForm> now = messageDeleteForm.bindFromRequest();
        EmpfangeneMessage.deleteMsgs(now.get().ids);
        //flash?
        return seeOther(routes.Messaging.showMessages(0));
    }

    public static Result deleteMessage(long id) {
        EmpfangeneMessage.find.ref(id).deleteMsg();
        //flash?
        return seeOther(routes.Messaging.showMessages(0));
    }
    public static Result deleteSendMessages() {
        Form<IdListForm> now = messageDeleteForm.bindFromRequest();
        Message.deleteSend(now.get().ids);
        //flash?
        return seeOther(routes.Messaging.showSendMessages(0));
    }
    public static Result deleteSendMessage(long id) {
        Message.find.ref(id).deleteSendMsg();
        //flash?
        return seeOther(routes.Messaging.showSendMessages(0));
    }


    public static Result liveBenutzerSearch(){

        StringBuilder s = new StringBuilder().append("[");
        List<Benutzer> tmp =Benutzer.liveSearch(DynamicForm.form().bindFromRequest().get("name"));
        if(tmp.isEmpty()) {
            return ok(Json.parse("[{\"label\":\"Kein Benutzer Gefunden\",\"valu\":-1}]")); //TODO(Alex):Suche Anpassen
        } else{
            for(Benutzer b :tmp){
                s.append("{\"label\":\"").append(b.fullName());
                s.append("\",\"valu\":\"").append(b.id).append("\"},");
            }
            s.deleteCharAt(s.length()-1).append("]");
            JsonNode j = Json.parse(s.toString());


            return ok(j);
        }
    }

}
