package models;
import java.lang.IllegalArgumentException;
import java.lang.String;
import java.util.*;
import java.util.Date;

import play.data.format.Formats;
import play.db.ebean.*;
import play.data.validation.Constraints.*;
import scala.xml.PrettyPrinter;

import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.core.models.Subject;
import org.springframework.util.ObjectUtils;
import play.Logger;
import play.data.validation.Constraints;
import play.db.ebean.*;
import scala.Option;
import securesocial.core.*;
import securesocial.core.java.SecureSocial;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
public class Bewertung extends Model{
    @Id
    public long id;

    public int wertung;

    @ManyToOne
    public Benutzer bewerter;
    @ManyToOne
    public Benutzer bewerteter;
    @ManyToOne
    public Ausleihe ausleihe;

    public static Finder<Long,Bewertung> find = new Finder(
            Long.class, Bewertung.class
    );

    public Bewertung(Benutzer bewerter, Benutzer bewerteter , Ausleihe ausleihe, int wertung){
        this.bewerter = bewerter;
        this.bewerteter = bewerteter;
        this.ausleihe = ausleihe;
        this.wertung = wertung;
    }
    public static double average(Benutzer benutzer){
        double summe = 0;
        if(find.where().eq("bewerteter", benutzer).findList().isEmpty())
        {return -1.0;}else{
        for(Bewertung b: find.where().eq("bewerteter", benutzer).findList()){
            summe = summe + b.wertung;
        }
        return (summe / find.where().eq("bewerteter", benutzer).findRowCount());
        }
    }
    /**
     * Returns Bewertung für den Bewerteten bei der Ausleihe oder null.
     *
     * @param  ausleihe  Ausleihe für die die Bewertung gefunden werden soll
     * @param  bewerteter Der Leiher oder Verleiher aus der Ausleihe
     * @return      Bewertung oder null
     */
    public static Bewertung getBewertungFor(Ausleihe ausleihe, Benutzer bewerteter){
        return Bewertung.find.where().eq("ausleihe",ausleihe).eq("bewerteter",bewerteter).findUnique();
    }
}
