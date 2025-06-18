package org.kivislime.weatherapp.session.exception;

public class QuotaExceededException extends RuntimeException {
    public QuotaExceededException(String format) {
        super(format);
    }
}
