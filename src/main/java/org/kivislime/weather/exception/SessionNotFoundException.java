package org.kivislime.weather.exception;

public class SessionNotFoundException extends RuntimeException {
    public SessionNotFoundException(String format) {
        super(format);
    }
}
