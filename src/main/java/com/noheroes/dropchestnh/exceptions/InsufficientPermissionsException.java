/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.exceptions;

/**
 *
 * @author PIETER
 */
public class InsufficientPermissionsException extends Exception {
    private String message;
    
    public InsufficientPermissionsException(String message) {
        this.message = message;
    }
    
    public InsufficientPermissionsException() {
        this.message = "You do not have permission to use this command";
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}
