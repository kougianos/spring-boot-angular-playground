package com.springboot.starter.dto;

import java.time.LocalDateTime;

public record TestDocumentResponse(
    String id,
    LocalDateTime dateCreated,
    LocalDateTime dateUpdated,
    String creator,
    Boolean booleanFlag,
    String textField,
    Integer numberField
) {
}
