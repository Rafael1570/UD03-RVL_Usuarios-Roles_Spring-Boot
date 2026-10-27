package org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class DatabaseErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseErrorHandler.class);
    @ExceptionHandler(DataAccessException.class)
    public ModelAndView databaseError(DataAccessException exception) {
        logger.error("Error de acceso a la base de datos", exception);
        ModelAndView view = new ModelAndView("error/database");
        view.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        return view;
    }
}
