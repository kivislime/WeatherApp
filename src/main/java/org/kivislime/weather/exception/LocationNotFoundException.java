package org.kivislime.weather.exception;

public class LocationNotFoundException extends RuntimeException {
    public LocationNotFoundException(String apiMessage) {
        super(apiMessage);
    }
}
