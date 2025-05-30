package org.kivislime.weather.exception;

public class QuotaExceededException extends RuntimeException {
    public QuotaExceededException(String format) {
        super(format);
    }
}
