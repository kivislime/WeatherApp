package org.kivislime.weather.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kivislime.weather.exception.*;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.servlet.ModelAndView;

import static org.assertj.core.api.Assertions.assertThat;


class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleBadRequest_ShouldReturn400View() {
        BadRequestException ex = new BadRequestException("bad request occurred");

        ModelAndView mav = handler.handleBadRequest(ex);

        assertThat(mav.getViewName()).isEqualTo("error");
        assertThat(mav.getModel()).containsEntry("statusCode", 400);
        assertThat(mav.getModel()).containsEntry("exceptionMessage", "Bad request");
    }

    @Test
    void handleLocationNotFound_ShouldReturn404View() {
        LocationNotFoundException ex = new LocationNotFoundException("cannot find");

        ModelAndView mav = handler.handleLocationNotFound(ex);

        assertThat(mav.getViewName()).isEqualTo("error");
        assertThat(mav.getModel()).containsEntry("statusCode", 404);
        assertThat(mav.getModel()).containsEntry("exceptionMessage", "Location not found");
    }

    @Test
    void handleSessionNotFound_ShouldReturn404ViewWithCustomMessage() {
        SessionNotFoundException ex = new SessionNotFoundException("session xyz missing");

        ModelAndView mav = handler.handleSessionNotFound(ex);

        assertThat(mav.getViewName()).isEqualTo("error");
        assertThat(mav.getModel()).containsEntry("statusCode", 404);
        assertThat(mav.getModel())
                .containsEntry("exceptionMessage", "The session was not found, please log in again");
    }

    @Test
    void handleUserNotFound_ShouldReturn404View() {
        UserNotFoundException ex = new UserNotFoundException("user 123 not found");

        ModelAndView mav = handler.handleUserNotFound(ex);

        assertThat(mav.getViewName()).isEqualTo("error");
        assertThat(mav.getModel()).containsEntry("statusCode", 404);
        assertThat(mav.getModel()).containsEntry("exceptionMessage", "User not found");
    }

    @Test
    void handleQuotaExceeded_ShouldReturn429View() {
        QuotaExceededException ex = new QuotaExceededException("too many requests");

        ModelAndView mav = handler.handleQuotaExceeded(ex);

        assertThat(mav.getViewName()).isEqualTo("error");
        assertThat(mav.getModel()).containsEntry("statusCode", 429);
        assertThat(mav.getModel()).containsEntry(
                "exceptionMessage",
                "The limit of requests to the service has been exceeded. Repeat in a minute"
        );
    }

    @Test
    void handleLocationLimitExceeded_ShouldReturn429View() {
        LocationLimitExceededException ex = new LocationLimitExceededException("limit 5 reached");

        ModelAndView mav = handler.handleLocationLimitExceeded(ex);

        assertThat(mav.getViewName()).isEqualTo("error");
        assertThat(mav.getModel()).containsEntry("statusCode", 429);
        assertThat(mav.getModel()).containsEntry("exceptionMessage", "Too many locations. Please remove one before adding another");
    }

    @Test
    void handleExternalApiError_ShouldReturn502View() {
        ExternalApiException ex = new ExternalApiException("api down");

        ModelAndView mav = handler.handleExternalApiError(ex);

        assertThat(mav.getViewName()).isEqualTo("error");
        assertThat(mav.getModel()).containsEntry("statusCode", 502);
        assertThat(mav.getModel()).containsEntry(
                "exceptionMessage",
                "The weather service is temporarily unavailable. Try again later"
        );
    }

    @Test
    void handleDataAccessError_ShouldReturn500View() {
        DataAccessException dae = new EmptyResultDataAccessException(1);

        ModelAndView mav = handler.handleDataAccessError(dae);

        assertThat(mav.getViewName()).isEqualTo("error");
        assertThat(mav.getModel()).containsEntry("statusCode", 500);
        assertThat(mav.getModel()).containsEntry(
                "exceptionMessage",
                "Oops something went wrong, try again later"
        );
    }

    @Test
    void handleAll_ShouldReturn500ViewForGenericException() {
        Exception ex = new Exception("unknown error");

        ModelAndView mav = handler.handleAll(ex);

        assertThat(mav.getViewName()).isEqualTo("error");
        assertThat(mav.getModel()).containsEntry("statusCode", 500);
        assertThat(mav.getModel()).containsEntry(
                "exceptionMessage",
                "Oops something went wrong, try again later"
        );
    }
}
