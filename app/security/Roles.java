package security;


import be.objectify.deadbolt.core.models.Role;

public enum Roles implements Role {
    user;

    @Override
    public String getName()
    {
        return name();
    }
}
