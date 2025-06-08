package org.kivislime.weather.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(basePackages = "org.kivislime.weather.controller")
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(BadRequestException.class)
    public ModelAndView handleBadRequest(BadRequestException ex) {
        log.error("Bad request: {}", ex.getMessage(), ex);
        return buildErrorView(400, "Bad request");
    }

    @ExceptionHandler(LocationNotFoundException.class)
    public ModelAndView handleLocationNotFound(LocationNotFoundException ex) {
        log.error("Location not found: {}", ex.getMessage(), ex);
        return buildErrorView(404, "Location not found");
    }

    @ExceptionHandler(SessionNotFoundException.class)
    public ModelAndView handleSessionNotFound(SessionNotFoundException ex) {
        log.error("Session not found: {}", ex.getMessage(), ex);
        return buildErrorView(404, "The session was not found, please log in again");
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ModelAndView handleUserNotFound(UserNotFoundException ex) {
        log.error("User not found: {}", ex.getMessage(), ex);
        return buildErrorView(404, "User not found");
    }

    @ExceptionHandler(QuotaExceededException.class)
    public ModelAndView handleQuotaExceeded(QuotaExceededException ex) {
        log.error("Quota exceeded: {}", ex.getMessage(), ex);
        return buildErrorView(429, "The limit of requests to the service has been exceeded. Repeat in a minute");
    }

    @ExceptionHandler(LocationLimitExceededException.class)
    public ModelAndView handleLocationLimitExceeded(LocationLimitExceededException ex) {
        log.error("Location limit exceeded: {}", ex.getMessage(), ex);
        return buildErrorView(429, "Too many locations. Please remove one before adding another");
    }

    @ExceptionHandler(ExternalApiException.class)
    public ModelAndView handleExternalApiError(ExternalApiException ex) {
        log.error("External API error: {}", ex.getMessage(), ex);
        return buildErrorView(502, "The weather service is temporarily unavailable. Try again later");
    }

    @ExceptionHandler(DataAccessException.class)
    public ModelAndView handleDataAccessError(DataAccessException ex) {
        log.error("DataAccess error: {}", ex.getMessage(), ex);
        return buildErrorView(500, "Oops something went wrong, try again later");
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleAll(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return buildErrorView(500, "Oops something went wrong, try again later");
    }

    private ModelAndView buildErrorView(int statusCode, String userMessage) {
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("statusCode", statusCode);
        mav.addObject("exceptionMessage", userMessage);
        return mav;
    }
}