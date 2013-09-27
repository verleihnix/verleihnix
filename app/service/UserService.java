/**
 * Copyright 2012 Jorge Aliss (jaliss at gmail dot com) - twitter: @jaliss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package service;

//import model.Rolle;
import models.Benutzer;
import models.Person;
import models.SecurityRole;
import models.TokenDB;
import play.Application;
import play.Logger;
import scala.Option;
import securesocial.core.Identity;
import securesocial.core.UserId;
import securesocial.core.java.BaseUserService;

import securesocial.core.java.Token;
import security.Roles;


public class UserService extends BaseUserService {


    public UserService(Application application) {
        super(application);
    }

    @Override
    public Identity doSave(Identity user) {
        Benutzer b = Benutzer.find.where().eq("uId",user.id().id()).eq("providerId",user.id().providerId()).findUnique();
        if(b==null){

            b = Benutzer.create(user);
            b.addRoles("user");

        }
        else{
            //save some changes?
            if(user.oAuth1Info().isDefined()){
                b.setOAuth1Info(user.oAuth1Info().get());
            }
            if(user.oAuth2Info().isDefined()){
                b.setOAuth2Info(user.oAuth2Info().get());
            }
            if(user.passwordInfo().isDefined()){
                b.setPasswordInfo(user.passwordInfo().get());
            }
            //p.firstName();
            //p.lastName();
            //p.email();
            b.setAuthMethod(user.authMethod());
            b.update();



        }
        // this sample returns the same user object, but you could return an instance of your own class
        // here as long as it implements the Identity interface. This will allow you to use your own class in the
        // protected actions and event callbacks. The same goes for the doFind(UserId userId) method.
        return b;
    }

    @Override
    public Identity doFind(UserId userId) {
        return Benutzer.find.where().eq("uId",userId.id()).eq("providerId",userId.providerId()).findUnique();
    }


    @Override
    public Identity doFindByEmailAndProvider(String email, String providerId) {
        return Benutzer.find.where().eq("providerId",providerId).ieq("email",email).findUnique();
    }

    @Override
    public void doSave(Token token) {
        TokenDB.create(token);
    }
    @Override
    public Token doFindToken(String tokenId) {
        TokenDB t= TokenDB.find.byId(tokenId);
        Token found = null;
        if(t != null){
            found = new Token();
            found.setUuid(t.uuid);
            found.setEmail(t.email);
            found.setCreationTime(t.creationTime);
            found.setExpirationTime(t.expirationTime);
            found.setIsSignUp(t.isSignUp);
        }
        return found;
    }

    @Override
    public void doDeleteToken(String uuid) {
        TokenDB.find.ref(uuid).delete();
    }

    @Override
    public void doDeleteExpiredTokens() {
         TokenDB.doDeleteExpiredTokens();
    }
}
