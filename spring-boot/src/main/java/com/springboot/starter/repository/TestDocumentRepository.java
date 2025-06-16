package com.springboot.starter.repository;

import com.springboot.starter.model.TestDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestDocumentRepository extends MongoRepository<TestDocument, String> {
}
