package com.bank.core.common.exception;

import com.bank.core.notification.exception.InvalidNotificationHistoryRequestException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(InvalidNotificationHistoryRequestException.class)
    public ProblemDetail handleInvalidNotificationHistoryRequest(InvalidNotificationHistoryRequestException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid notification history request", exception.getMessage());
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create("https://bank.example/problems/" + status.value()));
        return problem;
    }
}
