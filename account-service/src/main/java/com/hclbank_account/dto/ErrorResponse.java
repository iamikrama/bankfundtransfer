package com.hclbank_account.dto;

import lombok.*; import java.time.LocalDateTime;

@Data @AllArgsConstructor
public class ErrorResponse {
    private int           status;
    private String        error;
    private String        message;
    private LocalDateTime timestamp;
}