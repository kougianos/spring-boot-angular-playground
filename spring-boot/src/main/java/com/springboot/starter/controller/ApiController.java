package com.springboot.starter.controller;

import com.springboot.starter.model.security.JwtResponse;
import com.springboot.starter.model.security.LoginRequest;
import com.springboot.starter.model.security.SignupRequest;
import com.springboot.starter.model.crud.TestDocumentRequest;
import com.springboot.starter.model.crud.TestDocumentResponse;
import com.springboot.starter.model.security.UserInfoResponse;
import com.springboot.starter.model.publicapi.DigitalOceanStatusResponse;
import com.springboot.starter.model.publicapi.DisneyCharactersResponse;
import com.springboot.starter.model.publicapi.DivisionData;
import com.springboot.starter.service.AuthService;
import com.springboot.starter.service.PublicApiService;
import com.springboot.starter.service.TestDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ApiController {

    private final AuthService authService;
    private final TestDocumentService testDocumentService;
    private final PublicApiService publicApiService;

    @PostMapping("/auth/signup")
    public ResponseEntity<Void> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        authService.registerUser(signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/auth/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    @GetMapping("/auth/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserInfoResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        UserInfoResponse userInfo = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(userInfo);
    }

    @PostMapping("/test-documents")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<TestDocumentResponse> createTestDocument(@RequestBody TestDocumentRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        TestDocumentResponse response = testDocumentService.createTestDocument(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/test-documents")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<TestDocumentResponse>> getAllTestDocuments() {
        List<TestDocumentResponse> documents = testDocumentService.getAllTestDocuments();
        return ResponseEntity.ok(documents);
    }
    
    @PutMapping("/test-documents/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<TestDocumentResponse> updateTestDocument(
            @PathVariable String id,
            @RequestBody TestDocumentRequest request) {
        TestDocumentResponse updatedDocument = testDocumentService.updateTestDocument(id, request);
        return ResponseEntity.ok(updatedDocument);
    }

    @DeleteMapping("/test-documents/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTestDocument(@PathVariable String id) {
        testDocumentService.deleteTestDocument(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/public/disney-characters")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<DisneyCharactersResponse> getDisneyCharacters() {
        return ResponseEntity.ok(publicApiService.getDisneyCharacters());
    }

    @GetMapping("/public/digital-ocean-status")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<DigitalOceanStatusResponse> getDigitalOceanStatus() {
        return ResponseEntity.ok(publicApiService.getDigitalOceanStatus());
    }

    @GetMapping("/public/bank-holidays")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, DivisionData>> getBankHolidays() {
        return ResponseEntity.ok(publicApiService.getBankHolidays());
    }
}
