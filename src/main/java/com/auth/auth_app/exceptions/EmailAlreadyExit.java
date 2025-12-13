package com.auth.auth_app.exceptions;

public class EmailAlreadyExit extends RuntimeException{
    public EmailAlreadyExit(String msg)
    {
        super(msg);
    }
    public EmailAlreadyExit()
    {
        super("Email is already Exits in Our Database");
    }
}
