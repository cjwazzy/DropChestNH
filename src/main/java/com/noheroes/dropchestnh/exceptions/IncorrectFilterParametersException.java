/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noheroes.dropchestnh.exceptions;

/**
 *
 * @author PIETER
 */
public class IncorrectFilterParametersException extends Exception {
    private String message;
    
    public IncorrectFilterParametersException(String message) {
        this.message = message;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}
