package com.springboot.starter.dto;

public record TestDocumentRequest(
    Boolean booleanFlag,
    String textField,
    Integer numberField
) {
}
