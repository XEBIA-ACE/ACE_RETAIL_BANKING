package com.bank.core.common.exception;

import java.time.Instant;

public record ErrorResponseDto(int status, String code, String message, Instant timestamp) {
}
