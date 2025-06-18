package org.kivislime.weatherapp.location.exception;

public class LocationNotFoundException extends RuntimeException {
    public LocationNotFoundException(String apiMessage) {
        super(apiMessage);
    }
}
