package org.kivislime.weatherapp.user.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String format) {
        super(format);
    }
}
