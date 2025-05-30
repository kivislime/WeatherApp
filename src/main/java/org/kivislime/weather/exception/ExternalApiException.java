package org.kivislime.weather.exception;

public class ExternalApiException extends RuntimeException {
    public ExternalApiException(String format) {
        super(format);
    }
}
