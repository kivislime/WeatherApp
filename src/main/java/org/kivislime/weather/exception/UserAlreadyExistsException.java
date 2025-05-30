package org.kivislime.weather.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String format) {
        super(format);
    }
}
