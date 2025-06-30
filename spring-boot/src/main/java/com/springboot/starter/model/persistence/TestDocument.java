package com.springboot.starter.model.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "test_documents")
public class TestDocument {
    @Id
    private String id;
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated;
    private String creator;
    private Boolean booleanFlag;
    private String textField;
    private Integer numberField;
}
