package com.springboot.starter.service;

import com.springboot.starter.dto.TestDocumentRequest;
import com.springboot.starter.dto.TestDocumentResponse;
import com.springboot.starter.model.TestDocument;
import com.springboot.starter.repository.TestDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
    
    public List<TestDocumentResponse> getAllTestDocuments() {
        return testDocumentRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }
    
    public TestDocumentResponse updateTestDocument(String id, TestDocumentRequest request) {
        TestDocument existingDocument = testDocumentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Test document not found with id: " + id));

        LocalDateTime now = LocalDateTime.now();
        
        existingDocument.setDateUpdated(now);
        existingDocument.setBooleanFlag(request.booleanFlag());
        existingDocument.setTextField(request.textField());
        existingDocument.setNumberField(request.numberField());
        
        TestDocument updatedDocument = testDocumentRepository.save(existingDocument);
        
        return mapToResponse(updatedDocument);
    }
    
    public void deleteTestDocument(String id) {
        if (!testDocumentRepository.existsById(id)) {
            throw new IllegalArgumentException("Test document not found with id: " + id);
        }
        testDocumentRepository.deleteById(id);
    }
    
    private TestDocumentResponse mapToResponse(TestDocument document) {
        return new TestDocumentResponse(
            document.getId(),
            document.getDateCreated(),
            document.getDateUpdated(),
            document.getCreator(),
            document.getBooleanFlag(),
            document.getTextField(),
            document.getNumberField()
        );
    }
}
