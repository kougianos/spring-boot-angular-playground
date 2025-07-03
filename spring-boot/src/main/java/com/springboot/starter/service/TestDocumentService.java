package com.springboot.starter.service;

import com.springboot.starter.model.crud.TestDocumentRequest;
import com.springboot.starter.model.crud.TestDocumentResponse;
import com.springboot.starter.model.persistence.TestDocument;
import com.springboot.starter.repository.TestDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestDocumentService {

    private final TestDocumentRepository testDocumentRepository;
    private final CacheService cacheService;

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
        
        // Invalidate cache after creation
        cacheService.evict("test-documents:all", creator);
        
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
        return getAllTestDocuments("system");
    }
    
    public List<TestDocumentResponse> getAllTestDocuments(String userId) {
        return cacheService.getOrSet(
            "test-documents:all",
            () -> testDocumentRepository.findAll().stream()
                    .map(this::mapToResponse)
                    .toList(),
            Duration.ofMinutes(5),
            userId
        );
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
        
        // Invalidate cache after update
        cacheService.evict("test-documents:all", existingDocument.getCreator());
        cacheService.evict("test-document:" + id, existingDocument.getCreator());
        
        return mapToResponse(updatedDocument);
    }
    
    public void deleteTestDocument(String id) {
        if (!testDocumentRepository.existsById(id)) {
            throw new IllegalArgumentException("Test document not found with id: " + id);
        }
        
        // Invalidate cache before deletion
        cacheService.evict("test-documents:all", "system");
        cacheService.evict("test-document:" + id, "system");
        
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
