package org.kivislime.weather.exception;

public class LocationLimitExceededException extends RuntimeException {
    public LocationLimitExceededException(String message) {
        super(message);
    }
}
