package com.bank.core.common.exception;

import com.bank.core.notification.service.SupportedLocales;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;
    private final Clock clock;

    public GlobalExceptionHandler(MessageSource messageSource, Clock clock) {
        this.messageSource = messageSource;
        this.clock = clock;
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDto> handleBusiness(BusinessException ex, HttpServletRequest request) {
        return build(ex.getStatus(), ex.getCode(), ex.getMessageKey(), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleTypeMismatch(HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", "error.invalidParameter", request);
    }

    private ResponseEntity<ErrorResponseDto> build(HttpStatus status, String code, String key,
                                                   HttpServletRequest request) {
        Locale locale = SupportedLocales.resolve(request.getParameter("lang"), request.getLocale());
        String message = messageSource.getMessage(key, null, key, locale);
        return ResponseEntity.status(status)
                .body(new ErrorResponseDto(status.value(), code, message, clock.instant()));
    }
}
