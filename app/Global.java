import be.objectify.deadbolt.core.models.Role;
import org.h2.tools.RunScript;
import play.*;
import play.db.DB;
import play.libs.*;
import com.avaje.ebean.Ebean;
import models.*;
import security.Roles;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.Object;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Global extends GlobalSettings {
    @Override
    public void onStart(Application app) {
        //
        if (SecurityRole.find.findRowCount() == 0) {
            for (Role name : Roles.values()) {
                SecurityRole role = new SecurityRole();
                role.name = name.getName();
                role.save();
            }
        }
        /*if (UserPermission.find.findRowCount() == 0) {
            UserPermission permission = new UserPermission();
            permission.value = "printers.edit";
            permission.save();
        }  */


        // Check if the database is empty
        if(Ebean.find(Benutzer.class).findRowCount() == 0) {

            Map<String,List<Object>> all = (Map<String,List<Object>>)Yaml.load("initial-data.yml");
            // Insert Benutzer

            Ebean.save(all.get("benutzer"));
            for(Benutzer b:Benutzer.all()){
                b.addRoles("user");
                b.save();
            }
            for(Object o: all.get("person")) {
                Person p = (Person)o;
                p.benutzer = Benutzer.find.byId(p.benutzer.id);
                Ebean.save(p);
            }


            // Insert ausleihe
            // Ebean.save(all.get("ausleihe"));
            for(Object p: all.get("ausleihe")) {
                // Insert the project/user relation
                Ausleihe a = (Ausleihe)p;
                a.leiher = Person.find.byId(a.leiher.id);
                a.verleiher= Person.find.byId( a.verleiher.id);
                a.item = Item.find.byId(a.item.id);
                Ebean.save(a);
            }


            Benutzer owner = Benutzer.find.byId(500L);

            BoardNotice notice1 = new BoardNotice();
            notice1.owner = owner;
            notice1.item = Item.find.byId(1001L);
            notice1.typ = NoticeTyp.Anbieten;
            notice1.bis = new Date(113,7,12);
            notice1.akzeptiertPost=true;
            notice1.akzeptiertUebergabe=true;
            BoardNotice.create(notice1);

            BoardNotice notice2 = new BoardNotice();
            notice2.owner = owner;
            notice2.item = Item.find.byId(1002L);
            notice2.typ = NoticeTyp.Anbieten;
            notice2.bis = new Date(113,7,3);
            notice2.akzeptiertPost=true;
            notice2.akzeptiertUebergabe=false;
            BoardNotice.create(notice2);

            BoardNotice notice3 = new BoardNotice();
            notice3.owner = Benutzer.find.byId(501L);
            notice3.item = Item.find.byId(1001L);
            notice3.typ = NoticeTyp.Suchen;
            notice3.bis = new Date(113,7,17);
            notice3.akzeptiertPost=false;
            notice3.akzeptiertUebergabe=true;
            notice3.uebergabeRadiusKM =10.0f;
            BoardNotice.create(notice3);

            BoardNotice notice4 = new BoardNotice();
            notice4.owner = Benutzer.find.byId(501L);
            notice4.item = Item.find.byId(1000L);
            notice4.typ = NoticeTyp.Suchen;
            notice4.bis = new Date(113,8,28);
            notice4.akzeptiertPost=false;
            notice4.akzeptiertUebergabe=true;
            notice4.uebergabeRadiusKM =10.0f;
            BoardNotice.create(notice4);

            Tag s=Tag.create("Schere");
            Tag sc=Tag.create("Schlauch");
            Tag g=Tag.create("Gartenwerkzeug");

            Item i=Item.find.byId(1000l);
            i.addTag(g);
            i.addTag(sc);

            i=Item.find.byId(1001l);
            i.addTag(g);
            i.addTag(s);
        }

        //füllt die PlzKoordinaten Table
        if(Ebean.find(PlzGeokoordinate.class).findRowCount() == 0) {
            Logger.debug("lade PlzGeokoordinate"+Paths.get(".").toAbsolutePath());
            try {
                BufferedReader reader = Files.newBufferedReader(Paths.get("conf/geoplz.csv"), Charset.forName("UTF-8"));
                String line=null;
                Pattern p= Pattern.compile("^([^;]+);([^;]+);([^;]+);([^;]+);([^;]+);([^;]+);([^;]+)$");
                while((line=reader.readLine())!=null){
                    Matcher m=p.matcher(line);
                    if(m.matches()){
                        PlzGeokoordinate.create(Long.valueOf(m.group(1)),m.group(2),m.group(3),m.group(4),m.group(5),Double.valueOf(m.group(6)),Double.valueOf(m.group(7)));
                    } else{
                        Logger.error("Match faild: \n"+line);
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            for(Benutzer b:Benutzer.all()){
                b.plz= PlzGeokoordinate.find.byId(b.id.equals(500l)?26715l:26719l);
                b.save();
            }
        }else {
            Logger.debug("PlzGeoKoordianten bereits geladen");
        }
    }
}