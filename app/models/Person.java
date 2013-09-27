package models;

import java.lang.*;
import java.lang.Long;
import java.lang.String;
import java.util.*;
import java.util.ArrayList;
import java.util.List;

import play.db.ebean.*;
import play.data.validation.Constraints.*;
import javax.persistence.*;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;

@Entity
public class Person extends Model {

    @Id
    public Long id;

    // Persistenz wird sichergestellt.
    // Wird eine Benutzer geloescht, werden auch seine Items geloescht
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    public List<Item> items;
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE},mappedBy = "leiher")
    public List<Ausleihe> geliehen;
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE},mappedBy = "verleiher")
    public List<Ausleihe> verliehen;
    @OneToOne()
    public Benutzer benutzer;

    @ManyToOne()
    public Benutzer owner;

    // find helper fuer Queries ueber den ORM
    public static Finder<Long,Person> find = new Finder(
            Long.class, Person.class
    );

    /**
     * hier den Namen der Person speichern, sofern die Person keinen Account hat
     */
    public String simpleName;

    public static Person Create(String name){
        Person p = new Person();
        p.simpleName = name;
        p.save();
        return p;
    }
    public static Person Create(Benutzer user){
        Person p = new Person();
        p.benutzer = user;
        p.save();
        user.person = p;
        user.update();
        return p;
    }

    /**
     * Der Name, der angezeigt werden soll (Vorname + Name oder Spitzname)
     * @return
     */
    public String getName(){
        if(benutzer != null)
            return benutzer.toString();
        else
            return simpleName;
    }

    public static List<Person> all() {
        return find.all();
    }
    public static void delete(Long id) {
        find.ref(id).delete();
    }

}
