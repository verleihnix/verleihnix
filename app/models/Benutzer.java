package models;

import java.lang.IllegalArgumentException;
import java.lang.Long;
import java.lang.String;
import java.util.*;

import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.core.models.Subject;
import com.avaje.ebean.Expr;
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
public class Benutzer extends Model implements Identity,Subject {


    @Id
    public Long id;
	public String name = "";
	public String vorname = "";
    public Date geburtsdatum;
    public Long bewertung;
    @Constraints.Email
	public String email;
	public String wohnort;
    @ManyToOne
    public PlzGeokoordinate plz;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, mappedBy="benutzer",fetch = FetchType.EAGER)
    public Person person;

    //Beziehungen
    @OneToMany(mappedBy="sender")
    public List<Message> gesendeteMessage;
    @OneToMany(mappedBy="empfaenger")
    public List<EmpfangeneMessage> empfangeneMessage;
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE},mappedBy = "owner")
    public List<BoardNotice> boardNotices;
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    public List<BoardNotice> interessiertAn;

    @ManyToMany(cascade={CascadeType.ALL})
    @JoinTable(name="Benutzer_Freund",
            joinColumns={@JoinColumn(name="benutzer_id")},
            inverseJoinColumns={@JoinColumn(name="friend_id")})
    public Set<Benutzer> freundesListe;

    @OneToMany(cascade={CascadeType.ALL},mappedBy = "owner")    // die Person die man für sich selber erzeugt hat
    public Set<Person> eigenePersonen;

	// find helper fuer Queries ueber den ORM
	public static Finder<Long,Benutzer> find = new Finder(
		Long.class, Benutzer.class
	);

	public static List<Benutzer> all() {
		return find.all();
	}


    public static Benutzer loggedInUser() {

        return (Benutzer) controllers.Application.ctx().args.get(SecureSocial.USER_KEY);

    }

    /**
     * Ruft die bekannten Kontakte dieser Person ab
     * @return
     */
    public List<Person> knownContacts() {
        //Dummy, später durch z.B. nur Bekannte Leute (oder Freunde) filtern
        List<Person> p= Person.find.all();
        p.remove(this.person);
        return p;
    }

    public static List<Benutzer> liveSearch(String search){
        return find.setMaxRows(12).where().icontains("vorname||' '||name", search).findList();//vorname+" "+name und dann suche
		//return find.setMaxRows(12).where().disjunction().add(Expr.icontains("vorname", search))add(Expr.icontains("name", search)).endJunction().findList(); // or search
		//return find.setMaxRows(12).where().disjunction().add(Expr.icontains("vorname", search))add(Expr.icontains("name", search)).endJunction().findList(); // or search with split string
    }

	public static void create(Benutzer benutzer) {
        if (benutzer.toString()=="" )
            throw new IllegalArgumentException("Sowohl Vor- als auch Nachname ist leer.");
        benutzer.save();
        Person.Create(benutzer);
	}
    public static Benutzer create(Identity user) {
        Benutzer b = new Benutzer();

        b.uId = user.id().id();
        b.providerId = user.id().providerId();
        b.name = user.lastName();
        b.vorname = user.firstName();
        b.avatarUrl = user.avatarUrl().isDefined()?user.avatarUrl().get():null;
        b.authMethod= user.authMethod().method();
        if(user.email().isDefined()){
            b.email = user.email().get();
        }
        if(user.passwordInfo().isDefined()){
           PasswordInfo pwInfo = user.passwordInfo().get();
            b.passwort = pwInfo.password();
            b.hasher = pwInfo.hasher();
            b.salt = pwInfo.salt().isDefined()?pwInfo.salt().get():null;
        }
        create(b);
        return b;
    }

	public static void delete(Long id) {
		find.ref(id).delete();
	}

    public void addFriend(Benutzer freund){
        freundesListe.add(freund);
        saveManyToManyAssociations("freundesListe");
    }
    public void removeFriend(Benutzer freund){
        freundesListe.remove(freund);
        saveManyToManyAssociations("freundesListe");
    }


    //private Benutzer(){   }

    public String uId;
    public String providerId;

    public String avatarUrl;
    public String authMethod;

    //oAuth1Info
    public String token;
    public String secret;

    //oAuth2Info
    public String accessToken;
    public String tokenType;
    public Integer expiresIn;
    public String refreshToken;

    public String hasher;
    public String passwort;
    public String salt;



    @Override
    public UserId id() {
        return new UserId(uId,providerId);
    }

    @Override
    public String firstName() {
        return vorname;
    }
    @Override
    public String lastName() {
        return name;
    }
    @Override
    public String fullName() {
        return vorname+" "+name;
    }
    @Override
    public Option<String> email() {
        if(email==null){
            return (Option<String>) Option.<String>empty();
        }   else{
            return (Option<String>)Option.apply(email);
        }
    }

    @Override
    public Option<String> avatarUrl() {
        if(avatarUrl==null){
            return (Option<String>) Option.<String>empty();
        }   else {
            return (Option<String>) Option.apply(avatarUrl);
        }
    }

    @Override
    public AuthenticationMethod authMethod() {
        return new AuthenticationMethod(authMethod);
    }
    public void setAuthMethod(AuthenticationMethod am){
        authMethod = am.method();
    }

    @Override
    public Option<OAuth1Info> oAuth1Info() {

        return (Option<OAuth1Info>) Option.<OAuth1Info>empty();//apply(oAuth1Info);
    }
    public void setOAuth1Info(OAuth1Info oa) {
        Logger.debug(oa.toString());
    }


    @Override
    public Option<OAuth2Info> oAuth2Info() {
        return (Option<OAuth2Info>) Option.<OAuth2Info>empty();//apply(oAuth2Info);
    }
    public void setOAuth2Info(OAuth2Info oa2) {
        Logger.debug(oa2.toString());
    }

    @Override
    public Option<PasswordInfo> passwordInfo() {
        Option<String> saltOption = (Option<String>) ((salt==null) ? Option.<String>empty(): Option.apply(salt));
        return (Option<PasswordInfo>) Option.apply(new PasswordInfo(hasher, passwort, saltOption));
    }
    public void setPasswordInfo(PasswordInfo pi) {
        Logger.debug(pi.toString());
        this.passwort=pi.password();
        this.hasher=pi.hasher();
        this.salt= pi.salt().isDefined()?pi.salt().get():null;

    }
    public String toString() {
        return (this.vorname + " " + this.name).trim();
    }

    @ManyToMany
    public List<SecurityRole> roles;

    @ManyToMany
    public List<UserPermission> permissions;


    @Override
    public List<? extends Role> getRoles()
    {
        return roles;
    }
    public void addRoles(String... roles){
        if(this.roles == null)this.roles= new ArrayList<>();
        for(String name : roles){
            this.roles.add(SecurityRole.findByName(name));
        }
        saveManyToManyAssociations("roles");
    }


    @Override
    public List<? extends Permission> getPermissions()
    {
        return permissions;
    }

    @Override
    public String getIdentifier()
    {
        return "";
    }
    //holt sich eine liste aller empfänger und dem sender einer nachricht
    public static Set<Benutzer> getEmpanger(Message m){

        Set<Benutzer> tmp =find.where().eq("empfangeneMessage.message",m).findSet();
        boolean empf=tmp.remove(loggedInUser());
        if(m.sender.equals(loggedInUser())){
            if( tmp.isEmpty()){
                tmp.add(m.sender);

            }

        }else{
            tmp.add(m.sender);
        }
        if(empf || m.sender.equals(loggedInUser())){
            return tmp;
        }
        else{
            return new HashSet<>();
        }
    }





}
