package covid19.stats.micasa.com.rest;

import covid19.stats.micasa.com.domain.exceptions.InvalidFilterException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;

@ControllerAdvice
public class ExceptionMapper extends ResponseEntityExceptionHandler {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = { Exception.class })
    void fileNotFound(Exception ex, WebRequest request) {
        ex.printStackTrace();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = { InvalidFilterException.class })
    String invalidQueryParameters(InvalidFilterException ex, WebRequest request) {
        return ex.getMessage();
    }

}
