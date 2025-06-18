package org.kivislime.weatherapp.session.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String format) {
        super(format);
    }
}
