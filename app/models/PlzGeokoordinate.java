package models;

import java.util.*;

import play.db.ebean.*;
import play.data.format.Formats.*;
import play.data.validation.Constraints.*;

import javax.persistence.Entity;
import javax.persistence.*;

@Entity
public class PlzGeokoordinate extends Model{

    @Id
    public Long id;

    public String plz;
    public String ort;
    public String bundesland;
    public String land;

    public double lat;
    public double lon;

    // find helper fuer Queries ueber den ORM
    public static Finder<Long,PlzGeokoordinate> find = new Finder(
            Long.class, PlzGeokoordinate.class
    );
    PlzGeokoordinate(long id,String plz,String ort,String bundesland,String land,double lat,double lon){
        this.id=id;
        this.plz=plz;
        this.ort=ort;
        this.bundesland=bundesland;
        this.land=land;
        this.lat=lat;
        this.lon=lon;
    }
    public static void create(long id,String plz,String ort,String bundesland,String land,double lat,double lon){
        new PlzGeokoordinate(id,plz,ort,bundesland,land, lat, lon).save();
    }


}