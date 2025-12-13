package com.auth.auth_app.exceptions;

public class UserNotFoundWithNameException extends RuntimeException{

    public UserNotFoundWithNameException(String msg){
        super(msg);
    }

    public UserNotFoundWithNameException()
    {
        super("User is not found with name");
    }
}
