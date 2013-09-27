package models;

import java.lang.String;
import java.util.*;

import play.db.ebean.*;
import play.data.format.Formats.*;
import play.data.validation.Constraints.*;

import javax.persistence.*;
import javax.persistence.AttributeOverrides;
import javax.persistence.metamodel.StaticMetamodel;

@Entity
public class Item extends Model {

	@Id
	public Long id;
	@Required @NonEmpty
	public String titel;
	public String beschreibung;
    @OneToMany()
    public List<BoardNotice> boardNotices;//Das Objekt, um welches es bei dieser Anzeige überhaupt geht
    @ManyToMany
    public List<Tag> tags;

	// find helper fuer Queries ueber den ORM
	public static Finder<Long,Item> find = new Finder(
		Long.class, Item.class
	);
	
	public static List<Item> all() {
		return find.all();
	}
	
	public static void create(Item item) {
        if (item.titel== null || item.titel.trim() == "" )
            throw new IllegalArgumentException("Der Titel ist leer.");
        item.save();
	}
    public Item(String titel){
        this.titel=titel;
    }
	
	public static void delete(Long id) {
		find.ref(id).delete();
	}

    public String toString(){
      return this.titel;
    }
    public void addTag(Tag tag){
        tags.add(tag);
        saveManyToManyAssociations("tags");
    }

}
