package models;


import com.avaje.ebean.Ebean;
import org.joda.time.DateTime;
import play.db.ebean.*;
import securesocial.core.java.Token;

import javax.persistence.*;

@Entity
public class TokenDB extends Model {
    @Id
    public String uuid;
    public String email;
    public DateTime creationTime;
    public DateTime expirationTime;
    public boolean isSignUp;

    public static Finder<String,TokenDB> find = new Finder(
            String.class, TokenDB.class
    );
    private TokenDB(Token t){
        uuid = t.getUuid();
        email = t.getEmail();
        creationTime = t.getCreationTime();
        expirationTime = t.getExpirationTime();
        isSignUp = t.getIsSignUp();
    }
    public static void create(Token token) {
        new TokenDB(token).save();
    }


    public static void doDeleteExpiredTokens() {
        Ebean.delete(TokenDB.class,TokenDB.find.where().le("expirationTime",DateTime.now()).findIds());
    }
}
