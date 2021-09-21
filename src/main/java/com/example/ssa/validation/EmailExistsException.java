package com.example.ssa.validation;

public class EmailExistsException extends Throwable {

    public EmailExistsException(String detailMessage) {
        super(detailMessage);
    }
}
