package forms;


import models.*;
import play.core.j.JavaHelpers;

import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import static play.data.validation.Constraints.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class WriteMessageForm {
    @Required
    @MinLength(3)
    public String subject;
    @Required
    @MinLength(3)
    public String message;
    @Required()
    public List<Benutzer> empfaenger;

    public MessageType typ=MessageType.Default;
    public Long refId=-1l;

    public WriteMessageForm(){

    }

    /**
     * Konstruktor zum vorbefüllen der Form
     * @param subject
     * @param message
     */
    private WriteMessageForm(String subject, String message,Collection<Benutzer> empfaenger,MessageType typ,long refId){
        this.subject=subject;
        this.message=message;
        this.empfaenger= new ArrayList<>(empfaenger);
        this.typ=typ;
        this.refId=refId;
    }

    public static WriteMessageForm reply(Message msg,Set<Benutzer> empf){
        return new WriteMessageForm("re:"+msg.subject,"\n\n"+msg.sender.name+": " +msg.message,empf,MessageType.Default,-1);
    }
    public static WriteMessageForm zuAusleihe(Ausleihe ausleihe, Benutzer empf){
        List<Benutzer> empfList=new ArrayList<>();
        empfList.add(empf);
        return new WriteMessageForm((ausleihe.verleiher.benutzer.equals(empf)?"Geliehenes Item: ":"Verliehenes Item: ")+ausleihe.item.titel,"",empfList,MessageType.Ausleihe,ausleihe.id);
    }

    public static WriteMessageForm zuBoardNotice(BoardNotice notice, Benutzer empf){

        List<Benutzer> empfList=new ArrayList<>();
        empfList.add(empf);

        return new WriteMessageForm( (notice.typ.equals(NoticeTyp.Anbieten)?"Anfrage zum angebotenem Item: ":"Interessiert an folgendem Item?: ")+notice.item.titel,"",empfList,MessageType.Notice,notice.id);
    }


}
