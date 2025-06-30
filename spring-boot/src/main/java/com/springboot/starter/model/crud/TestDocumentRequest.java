package com.springboot.starter.model.crud;

public record TestDocumentRequest(
    Boolean booleanFlag,
    String textField,
    Integer numberField
) {
}
