package org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.services;

import lombok.Getter;

@Getter
public class FormException extends RuntimeException {
    private final String field;
    private final String code;

    public FormException(String field, String code) {
        super(code);
        this.field = field;
        this.code = code;
    }
}
