package models;
import java.lang.String;
import java.util.*;


import forms.WriteMessageForm;
import org.joda.time.DateTime;
import play.Logger;
import play.db.ebean.*;

import javax.persistence.*;

@Entity
public class Message extends Model {
    @Id
    public long id;
    public String subject;
    @Lob
    public String message;
    public DateTime dateTime;

    public int referred;

    public boolean senderDeleted=false;

    @OneToMany(cascade = CascadeType.REMOVE )
    public List<EmpfangeneMessage> empfaenger;
    @ManyToOne
    public Benutzer sender;

    //Message typ und refference zu einer Ausleihe oder BoardNotice

    public MessageType typ;
    public Long refId;


    private static int pageSize=15;

    // find helper fuer Queries ueber den ORM
    public static Finder<Long,Message> find = new Finder(
            Long.class, Message.class
    );


    //public void sendSystem(String from,Type t,Benutzer to, String subject, String message) {  }

    public Message(String subject,String message,int referred,MessageType typ, long refId){
        this.subject=subject;
        this.message=message;
        this.referred=referred;
        this.typ=typ;
        this.refId=refId;
        sender = Benutzer.loggedInUser();
        dateTime = DateTime.now();
    }

    public static void send(WriteMessageForm newMsg){
        Set<Benutzer >empf =new HashSet<>(newMsg.empfaenger);
        Message m = new Message(newMsg.subject,newMsg.message,empf.size()+1,newMsg.typ,newMsg.refId);
        m.save();
        for(Benutzer b:empf){
            new EmpfangeneMessage(b,m).save();

        }

    }

    public static List<Message> getSendMessages(int page){
        Benutzer b = Benutzer.loggedInUser();
        return find.setMaxRows(pageSize).setFirstRow(page*pageSize).fetch("empfaenger").where().eq("sender",b).eq("senderDeleted", false).findList();
    }
    public static int getPageCount() {
        return Math.max((int)Math.ceil(find.where().eq("sender",Benutzer.loggedInUser()).eq("senderDeleted",false).findRowCount()/(double)pageSize),1);
    }

    public static Message readMessage(long id){
        Message m = find.byId(id);
        if(m!= null && m.owner()){
            return m;
        }
        return null;
    }

    public static void deleteSend(List<Long> msgIds){
        for(Long id: msgIds){
            find.ref(id).deleteSendMsg();
        }

    }
    public static void deleteAllSend(){
        for(Message m: find.fetch("empfaenger").where().eq("sender",Benutzer.loggedInUser()).eq("senderDeleted",false).findList()){
            m.deleteSendMsg();
        }

    }



    public void deleteSendMsg(){
        if(owner()){
        senderDeleted=true;
        deleteMsg();
        }
    }
    @Transactional
    void deleteMsg(){
        referred--;
        if (referred==0){
            delete();
        }else{
            update();
        }
    }
    private boolean owner(){
        return sender.equals(Benutzer.loggedInUser()) && !senderDeleted;
    }

    public static Message replyMessage(Long msgid) {
        return find.byId(msgid);
    }
}
