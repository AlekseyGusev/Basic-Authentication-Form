package com.example.baf.validation;

public class EmailExistsException extends Throwable {

    public EmailExistsException(String detailMessage) {
        super(detailMessage);
    }
}
