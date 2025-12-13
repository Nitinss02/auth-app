package com.auth.auth_app.exceptions;

public class UserIdIsNotValid extends RuntimeException{
    public UserIdIsNotValid(String msg)
    {
        super(msg);
    }
    public UserIdIsNotValid()
    {
        super("User id is not valid pleases check again");
    }
}
