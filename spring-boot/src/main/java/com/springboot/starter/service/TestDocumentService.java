package com.springboot.starter.service;

import com.springboot.starter.dto.TestDocumentRequest;
import com.springboot.starter.dto.TestDocumentResponse;
import com.springboot.starter.model.TestDocument;
import com.springboot.starter.repository.TestDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TestDocumentService {

    private final TestDocumentRepository testDocumentRepository;

    public TestDocumentResponse createTestDocument(TestDocumentRequest request, String creator) {
        LocalDateTime now = LocalDateTime.now();
        
        TestDocument testDocument = TestDocument.builder()
                .dateCreated(now)
                .dateUpdated(now)
                .creator(creator)
                .booleanFlag(request.booleanFlag())
                .textField(request.textField())
                .numberField(request.numberField())
                .build();
                
        TestDocument savedDocument = testDocumentRepository.save(testDocument);
        
        return new TestDocumentResponse(
            savedDocument.getId(),
            savedDocument.getDateCreated(),
            savedDocument.getDateUpdated(),
            savedDocument.getCreator(),
            savedDocument.getBooleanFlag(),
            savedDocument.getTextField(),
            savedDocument.getNumberField()
        );
    }
}
