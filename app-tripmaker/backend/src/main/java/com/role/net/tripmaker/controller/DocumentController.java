package com.role.net.tripmaker.controller;

import com.role.net.tripmaker.dto.document.CreateDocumentRequest;
import com.role.net.tripmaker.dto.document.DocumentResponse;
import com.role.net.tripmaker.entity.TripDocument;
import com.role.net.tripmaker.entity.User;
import com.role.net.tripmaker.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips/{groupId}/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    public ResponseEntity<DocumentResponse> createDocument(
            @PathVariable Long groupId,
            @Valid @RequestBody CreateDocumentRequest request,
            @AuthenticationPrincipal User loggedUser) {
        TripDocument saved = documentService.createDocument(groupId, request, loggedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(DocumentResponse.from(saved));
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getDocuments(
            @PathVariable Long groupId,
            @AuthenticationPrincipal User loggedUser) {
        List<DocumentResponse> responses = documentService.getDocuments(groupId, loggedUser).stream()
                .map(DocumentResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long groupId,
            @PathVariable Long documentId,
            @AuthenticationPrincipal User loggedUser) {
        documentService.deleteDocument(groupId, documentId, loggedUser);
        return ResponseEntity.noContent().build();
    }
}
