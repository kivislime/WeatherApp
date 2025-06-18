package org.kivislime.weatherapp.weather.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String format) {
        super(format);
    }
}
