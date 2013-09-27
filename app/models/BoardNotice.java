package models;

import com.avaje.ebean.*;
import play.Logger;
import play.data.validation.Constraints;
import play.db.ebean.*;
import play.data.format.Formats.*;
import play.data.validation.Constraints.*;
import scala.Option;

import javax.persistence.*;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.OneToOne;
import javax.persistence.Query;
import java.lang.*;
import java.lang.NullPointerException;
import java.lang.String;
import java.util.*;


import static java.lang.Math.*;
import static java.lang.Math.cos;
import static java.lang.Math.toRadians;


@Entity
public class BoardNotice extends Model {

    @Id
    public Long id;

    //Attribute
    public NoticeTyp typ; //Suchen oder anbieten


    public Date bis; //Anzeige ist maximal aktiv bis...
    public String kommentar;
    public boolean akzeptiertPost;
    public boolean akzeptiertUebergabe;
    public String akzeptiertSonstiges = "";
    public float uebergabeRadiusKM = 0.0F;
    private boolean aktiv = true;
    //Angebote/Suche nur für freunde zugänglichmachen Todo: Form um checkbox erweitern
    public boolean nurAnFreunde;

    public String getTitle(){
        return item.titel;
    }
    public String getBeschreibung(){
        return item.beschreibung;
    }
    //Beziehungen
    @ManyToOne
    public Item item;//Das Objekt, um welches es bei dieser Anzeige überhaupt geht
    @ManyToOne
    public Benutzer owner; //Die Person, die die Anzeige erstellt hat.
    @ManyToMany
    public List<Benutzer> interessenten;

    // find helper fuer Queries ueber den ORM
    public static Finder<Long,BoardNotice> find = new Finder(
            Long.class, BoardNotice.class
    );


    public static void create(BoardNotice boardNotice){
        if(boardNotice.owner.id==null)
            throw new IllegalArgumentException("Der Ersteller (owner) der Anzeige wurde nicht angegeben");
        if(boardNotice.item.id==null)
            throw new IllegalArgumentException("BoardNoticeItem wurde nicht angegeben");
        if(boardNotice.bis==null)
            throw new NullPointerException("\"bis\" wurde nicht angegeben");

        //Evtl. ist bislang nur die ID der jeweiligen Benutzer bekannt, daher den Rest nachtragen
        boardNotice.owner = Benutzer.find.byId(boardNotice.owner.id);
        boardNotice.item = Item.find.byId(boardNotice.item.id);

        boardNotice.item.saveManyToManyAssociations("tags");
        boardNotice.owner.boardNotices.add(boardNotice);

        boardNotice.owner.update();
        boardNotice.item.save(); //evtl. wurde das Item neu erstellt
        boardNotice.save();
    }

    public void addInteressent(Benutzer wer){
        if(wer==null)
            throw new IllegalArgumentException("Interessent ist Null!");

        if (! interessenten.contains(wer)){
            interessenten.add(wer);
            wer.interessiertAn.add(this);
            wer.update();
            this.update();
        }
    }

    public void removeInteressent(Benutzer wer){
        if(wer==null)
            throw new IllegalArgumentException("Interessent ist Null!");

        if (interessenten.contains(wer)){
            interessenten.remove(wer);
            wer.interessiertAn.remove(this);
            wer.update();
            this.update();
        }
    }

    public boolean istAktiv(){
        return aktiv && bis.after(new Date());
    }

    public boolean istArchive(){
        return !istAktiv();
    }

    public void archivieren(){
        aktiv = false;
        this.update();
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }

    static final long ONE_HOUR = 60 * 60 * 1000L;
    public String restzeit(){

        Date today = new Date();
        if(istAktiv()){
            Long rest = new Long((bis.getTime() - today.getTime() + ONE_HOUR) / (ONE_HOUR * 24));

            return rest + (rest==1 ? " Tag" : " Tage");
        }else{
            return "Beendet";
        }
    }

    @Transient
    public double distance=0;

    private static final int itemsPerPage=6;

    //für die distanz berechnungen
    private static final double latToKm = 111.132;
    private static final double EarthRadius = 6378.1; // kilometer
    private static double getLonOffset(double distance,double lat){
        return distance/abs(cos(toRadians(lat))*latToKm);
    }
    private static double getLatOffset(double distance){
        return (distance/latToKm);
    }

    /**
     * Default search
     * @param typ
     * @param page
     */
    public static Page<BoardNotice> search(NoticeTyp typ,int page){
        return search("", new ArrayList<Tag>(), typ, Option.<Boolean>empty(),false, Option.<Double>empty(), page);
    }

    public static Page<BoardNotice> search(String searchWord,List<Tag> tags,NoticeTyp typ,Option<Boolean> post,boolean friendsOnly, Option<Double> abholRadius, int page){
        Benutzer logedinUser = Benutzer.loggedInUser();
        ExpressionList<BoardNotice> query = find.where();
        //Angebote/Suche nur für freunde zugänglich
        if(logedinUser!= null){
            query.raw("(nurAnFreunde = false or ? in (" +
                      "select f.friend_id from Benutzer_Freund f where f.benutzer_id = owner and f.friend_id = ?) )"
                      ,new Long[]{logedinUser.id,logedinUser.id}
            );
        }else{
            query.eq("nurAnFreunde",false);
        }
        //nur anzeigen wenn Notice vom einem freund des suchend nutzers ist
        if(friendsOnly && logedinUser!=null){
            query.where().in("owner",logedinUser.freundesListe);
        }

        // filtert eigene Notices raus
        //query.where().eq("typ", typ).ne("owner", Benutzer.loggedInUser());

        // nur aktivie Notices anzeigen
        query.where().eq("aktiv", true).gt("bis",new Date());

        // Typ (Suchen oder Anbieten)
        query.where().eq("typ", typ);


        // Titel
        if(searchWord != null && !searchWord.isEmpty()){
            query.where().icontains("item.titel", searchWord);
        }
        // Tags
        if(tags!=null){

            for(Tag tag:tags){

                query.where().in("item.id",Ebean.createQuery(Item.class).where().eq("tags", tag).findIds() );//find.where().eq("item.tags", tag).select("id")
            }

        }
        //Post?
        if(post.isDefined()){
            query.where().eq("akzeptiertPost", post.get());
        }
        //distanz  (über boundingbox)
        double distance=0;

        if(abholRadius.isDefined()){//boundingbox da schnelere suche
            distance = abholRadius.get();
            PlzGeokoordinate ge=Benutzer.loggedInUser().plz;
            double lonOff=  getLonOffset(distance,ge.lat);
            double latOff = getLatOffset(distance);
            query.where().eq("akzeptiertUebergabe", true).between("owner.plz.lat",ge.lat-latOff,ge.lat+latOff).between("owner.plz.lon",ge.lon-lonOff,ge.lon+lonOff);
        }

        //ergebnise anzahl
        int rowcount=query.findRowCount();
        // ergebnis (eine seite)
        List<BoardNotice> list=query.setFirstRow(itemsPerPage*(page-1)).setMaxRows(itemsPerPage).findList();

        if(logedinUser!=null && logedinUser.plz!=null){
            //berechnet die genaue distanz und filtert zu weit entfernte aus
            Iterator<BoardNotice> it = list.iterator();
            PlzGeokoordinate ge=logedinUser.plz;
            for(;it.hasNext();){
                BoardNotice b= it.next();
                //genaue berechnung der distanz
                b.setDistnace(ge.lat,ge.lon);
                if(abholRadius.isDefined() && b.distance> distance)  {
                    it.remove();
                }
            }
        }
        return new Page(list,rowcount,Math.max(1, (int)Math.ceil((float)rowcount / itemsPerPage)));

    }

    public void setDistnace(double LoggedInUserLat,double LoggedInUserLon) {
        if(owner.plz!=null){
            distance= acos( sin(toRadians(owner.plz.lat)) * sin(toRadians(LoggedInUserLat)) + cos(toRadians(owner.plz.lat)) * cos(toRadians(LoggedInUserLat)) * cos(toRadians(owner.plz.lon) - toRadians(LoggedInUserLon))) * EarthRadius;
        }else{
            distance= Double.NaN;
        }

    }

    /**
     * Hilfs klasse zur leichten rückgabe einer Seite der Suche + max ergebnisen + seiten zahl
     * @param <T>
     */
    public static class Page<T> {
        public List<T> values;
        public int rowcount;
        public int pages;

        public Page(List<T> values, int rowcount, int pages){
            this.values=values;
            this.rowcount=rowcount;
            this.pages=pages;
        }
    }

}