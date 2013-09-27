package models;

import java.lang.IllegalArgumentException;
import java.lang.String;
import java.util.*;
import java.util.Date;

import play.Logger;
import play.data.format.Formats;
import play.db.ebean.*;
import play.data.validation.Constraints.*;
import scala.xml.PrettyPrinter;

import javax.persistence.*;

@Entity
public class Ausleihe extends Model {

	@Id
	public Long id;
	public Date von; //Ausleihedatum (nie Null, trotzdem nicht Reqiered, da nicht im Formular)
	public Date bis = null; //Wann wurde es tatsächlich zurückgegeben (Rückgabedatum)?
    public Date frist; //Wann soll es spätestens zurückgegeben werden (Rückgabefrist)?
    public String kommentar;
	// wurde das Item zurueckgegeben / zurueckerhalten
	public Boolean abgeschlossen;
	@Required @ManyToOne
	public Item item;
    @ManyToOne
    public Person verleiher;
    @ManyToOne
    public Person leiher;

	// find helper fuer Queries ueber den ORM
	public static Finder<Long,Ausleihe> find = new Finder(
		Long.class, Ausleihe.class
	);
	
	public static List<Ausleihe> all() {
		return find.all();
	}
	
	public static void create(Ausleihe ausleihe){
        if(ausleihe.leiher.id==null)
            throw new IllegalArgumentException("Der Leiher wurde nicht angegeben");
        if(ausleihe.verleiher.id==null)
                throw new IllegalArgumentException("Der Verleiher wurde nicht angegeben");

        //Evtl. ist bislang nur die ID der jeweiligen Benutzer bekannt, daher den Rest nachtragen
        ausleihe.leiher = Person.find.byId(ausleihe.leiher.id);
        ausleihe.verleiher = Person.find.byId(ausleihe.verleiher.id);
        if (ausleihe.leiher.benutzer == null && ausleihe.verleiher.benutzer == null)
            throw new IllegalArgumentException("Der Leiher und/ oder der Verleiher muss einen Account besitzen");


        ausleihe.verleiher.items.add(ausleihe.item);
        if (ausleihe.von == null)
            ausleihe.von = Calendar.getInstance().getTime();//Standard-Ausleihedatum

        ausleihe.verleiher.save(); //Beim Ausleihen wurde evtl. der Verleiher neu erstellt
        ausleihe.leiher.save(); //Bei Verleihen wurde evtl. der Leiher neu erstellt
        ausleihe.save();
    }

    public void bewerten(int wertung){
        if(leiher.benutzer.equals(Benutzer.loggedInUser())){
            bewertenVerleiher(wertung);
        }else{bewertenLeiher(wertung);}
    }
    private void bewertenVerleiher(int wertung){
        if(verleiherBewertet()){
            //bereitzs bewertet vileicht bewertung ändern sonst nix tun
        }else{

            new Bewertung(Benutzer.loggedInUser(), verleiher.benutzer, this,wertung).save();
        }
    }
    private void bewertenLeiher(int wertung){
        if(leiherBewertet()){
            //bereitzs bewertet vileicht bewertung ändern sonst nix tun
        }else{
            new Bewertung(Benutzer.loggedInUser(), leiher.benutzer, this,wertung).save();
        }
    }
    public boolean leiherBewertet(){
        return Bewertung.getBewertungFor(this,leiher.benutzer)!=null;

    }
    public boolean verleiherBewertet(){
        return Bewertung.getBewertungFor(this,verleiher.benutzer)!=null;
    }

    public int bewertungLeiher(){
       Bewertung tmp= Bewertung.getBewertungFor(this,leiher.benutzer);
       return tmp!=null?tmp.wertung:-1;
    }
    public int bewertungVerleiher(){
        Bewertung tmp= Bewertung.getBewertungFor(this,verleiher.benutzer);
        return tmp!=null?tmp.wertung:-1;
    }




    public static void rueckgeben(Long id) {
        Ausleihe elem = find.ref(id);
        elem.bis = Calendar.getInstance().getTime(); //Rückgabe-Datum auf Heute setzen
        elem.update();
    }

	public static void delete(Long id) {
		find.ref(id).delete();
	}

}
