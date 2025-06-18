package org.kivislime.weatherapp.location.exception;

public class LocationLimitExceededException extends RuntimeException {
    public LocationLimitExceededException(String message) {
        super(message);
    }
}
