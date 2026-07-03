package com.role.net.tripmaker.service;

import com.role.net.tripmaker.dto.document.CreateDocumentRequest;
import com.role.net.tripmaker.entity.Group;
import com.role.net.tripmaker.entity.TripDocument;
import com.role.net.tripmaker.entity.User;
import com.role.net.tripmaker.repository.DocumentRepository;
import com.role.net.tripmaker.repository.TripGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final TripGroupRepository groupRepository;

    public DocumentService(DocumentRepository documentRepository, TripGroupRepository groupRepository) {
        this.documentRepository = documentRepository;
        this.groupRepository = groupRepository;
    }

    private Group validateMembership(Long groupId, User loggedUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Viagem não encontrada."));
        if (!group.hasMember(loggedUser.getId().toString())) {
            throw new IllegalArgumentException("Você não é membro desta viagem.");
        }
        return group;
    }

    @Transactional
    public TripDocument createDocument(Long groupId, CreateDocumentRequest request, User loggedUser) {
        Group group = validateMembership(groupId, loggedUser);

        TripDocument doc = TripDocument.builder()
                .group(group)
                .creator(loggedUser)
                .title(request.title())
                .url(request.url())
                .category(request.category())
                .build();

        return documentRepository.save(doc);
    }

    @Transactional(readOnly = true)
    public List<TripDocument> getDocuments(Long groupId, User loggedUser) {
        validateMembership(groupId, loggedUser);
        return documentRepository.findByGroupIdOrderByCreatedAtDocDesc(groupId);
    }

    @Transactional
    public void deleteDocument(Long groupId, Long documentId, User loggedUser) {
        validateMembership(groupId, loggedUser);
        TripDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Documento não encontrado."));
        
        if (!doc.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("Documento não pertence a esta viagem.");
        }
        documentRepository.delete(doc);
    }
}
