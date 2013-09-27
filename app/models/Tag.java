package models;

import java.util.*;

import play.db.ebean.*;
import play.data.format.Formats.*;
import play.data.validation.Constraints.*;

import javax.persistence.Entity;
import javax.persistence.*;

@Entity
public class Tag extends Model{

    @Id
    public String name;

    public String desc;
    @ManyToMany
    public List<Item> items;
    // find helper fuer Queries ueber den ORM
    public static Finder<String,Tag> find = new Finder(
            String.class, Tag.class
    );
    Tag(String name){
        this.name=name;
    }
    public static Tag create(String name){
        Tag t=new Tag(name);
        t.save();
        return t;
    }



}
