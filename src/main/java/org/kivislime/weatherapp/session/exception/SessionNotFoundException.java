package org.kivislime.weatherapp.session.exception;

public class SessionNotFoundException extends RuntimeException {
    public SessionNotFoundException(String format) {
        super(format);
    }
}
