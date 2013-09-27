package models;
import java.util.*;

import play.db.ebean.*;

import javax.persistence.*;

@Entity
public class EmpfangeneMessage extends Model {
    @Id
    public long id;

    public boolean read=false;
    public boolean deleted=false;
    @ManyToOne()
    public Message message;
    @ManyToOne()
    public Benutzer empfaenger;

    private static int pageSize=15;


    // find helper fuer Queries ueber den ORM
    public static Finder<Long,EmpfangeneMessage> find = new Finder(
            Long.class, EmpfangeneMessage.class
    );
    public EmpfangeneMessage(Benutzer empfaenger, Message message){
        this.message=message;
        this.empfaenger =empfaenger;
    }

    public static List<EmpfangeneMessage> getMessages(int page){
        return find.setMaxRows(pageSize).setFirstRow(page*pageSize).fetch("message","subject,dateTime,sender").where().eq("empfaenger", Benutzer.loggedInUser()).eq("deleted",false).findList();
    }

    public static List<EmpfangeneMessage> getNewMessages(int page){
        return find.setMaxRows(pageSize).setFirstRow(page*pageSize).where().eq("empfaenger", Benutzer.loggedInUser()).eq("deleted",false).eq("read",false).findList();
    }
    public static int getPageCount(){
        return Math.max((int)Math.ceil(find.where().eq("empfaenger",Benutzer.loggedInUser()).eq("deleted",false).findRowCount()/(double)pageSize),1);
    }
    public static Message readMessage(long id){
        EmpfangeneMessage msg =find.byId(id);
        if(msg != null && msg.owner()){
            msg.read  =true;
            msg.update();
            return msg.message;
        }
        return null;
    }

    //public static List<Message>  getSystemMessages(){return null;}
    public static void deleteMsgs(List<Long> emsgIds){
        for(Long id: emsgIds){
            find.ref(id).deleteMsg();
        }

    }
    //public static void deleteAllReadMsgs(){    }

    public void deleteMsg(){
        if(owner()){
            deleted=true;
            update();
            message.deleteMsg();
        }
    }
    private boolean owner(){
        return empfaenger.equals(Benutzer.loggedInUser())&& !deleted;
    }

}