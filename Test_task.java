import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DocumentManager {

    private final List<Document> documents = new ArrayList<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        // Check if the document already has an ID
        if (document.getId() == null || document.getId().isEmpty()) {
            // Generate a unique ID
            document = document.toBuilder()
                    .id(UUID.randomUUID().toString())
                    .created(Instant.now()) // Set created time if it's a new document
                    .build();
        }
        // Remove the existing document with the same ID if it exists
        documents.removeIf(doc -> doc.getId().equals(document.getId()));
        // Add the document to the list
        documents.add(document);
        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        return documents.stream()
                .filter(doc -> matchesRequest(doc, request))
                .toList();
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return documents.stream()
                .filter(doc -> doc.getId().equals(id))
                .findFirst();
    }

    private boolean matchesRequest(Document document, SearchRequest request) {
        if (request.getTitlePrefixes() != null && !request.getTitlePrefixes().isEmpty()) {
            boolean matchesTitle = request.getTitlePrefixes().stream()
                    .anyMatch(prefix -> document.getTitle().startsWith(prefix));
            if (!matchesTitle) return false;
        }

        if (request.getContainsContents() != null && !request.getContainsContents().isEmpty()) {
            boolean matchesContent = request.getContainsContents().stream()
                    .anyMatch(content -> document.getContent().contains(content));
            if (!matchesContent) return false;
        }

        if (request.getAuthorIds() != null && !request.getAuthorIds().isEmpty()) {
            boolean matchesAuthor = request.getAuthorIds().stream()
                    .anyMatch(authorId -> document.getAuthor().getId().equals(authorId));
            if (!matchesAuthor) return false;
        }

        if (request.getCreatedFrom() != null && document.getCreated().isBefore(request.getCreatedFrom())) {
            return false;
        }

        if (request.getCreatedTo() != null && document.getCreated().isAfter(request.getCreatedTo())) {
            return false;
        }

        return true;
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}
