package org.kivislime.weather.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String format) {
        super(format);
    }
}
