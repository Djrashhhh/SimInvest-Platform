package com.example.MicroInvestApp.impl.learning;

import com.example.MicroInvestApp.domain.enums.ContentCategory;
import com.example.MicroInvestApp.domain.enums.ContentType;
import com.example.MicroInvestApp.domain.enums.DifficultyLevel;
import com.example.MicroInvestApp.domain.learning.EducationalContent;
import com.example.MicroInvestApp.dto.edcontent.CreateEducationalContentRequestDTO;
import com.example.MicroInvestApp.dto.edcontent.EducationalContentResponseDTO;
import com.example.MicroInvestApp.dto.edcontent.UpdateEducationalContentRequestDTO;
import com.example.MicroInvestApp.dto.edcontent.UserProgressDTO;
import com.example.MicroInvestApp.exception.learning.ContentNotFoundException;
import com.example.MicroInvestApp.repositories.learning.EducationalContentRepository;
import com.example.MicroInvestApp.service.learning.EducationalContentService;
import com.example.MicroInvestApp.service.learning.UserProgressService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of EducationalContentService
 * Handles all educational content-related business logic
 */
@Service
@Transactional
public class EducationalContentServiceImpl implements EducationalContentService {

    private static final Logger logger = LoggerFactory.getLogger(EducationalContentServiceImpl.class);

    private final EducationalContentRepository contentRepository;
    private final UserProgressService userProgressService;
    private final ObjectMapper objectMapper;

    @Autowired
    public EducationalContentServiceImpl(EducationalContentRepository contentRepository,
                                         UserProgressService userProgressService,
                                         ObjectMapper objectMapper) {
        this.contentRepository = contentRepository;
        this.userProgressService = userProgressService;
        this.objectMapper = objectMapper;
    }

    @Override
    public EducationalContentResponseDTO createContent(CreateEducationalContentRequestDTO request) {
        logger.info("Creating educational content with title: {}", request.getTitle());

        EducationalContent content = new EducationalContent(
                request.getTitle(),
                request.getDescription(),
                ContentCategory.valueOf(request.getCategory().toUpperCase()),
                DifficultyLevel.valueOf(request.getDifficultyLevel().toUpperCase()),
                ContentType.valueOf(request.getContentType().toUpperCase()),
                request.getContentUrl()
        );

        // Set additional fields
        if (request.getDurationMinutes() != null) {
            content.setDurationMinutes(request.getDurationMinutes());
        }
        if (request.getAuthor() != null) {
            content.setAuthor(request.getAuthor());
        }
        if (request.getSource() != null) {
            content.setSource(request.getSource());
        }
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            content.setTags(convertListToJson(request.getTags()));
        }
        if (request.getPrerequisites() != null && !request.getPrerequisites().isEmpty()) {
            content.setPrerequisites(convertListToJson(request.getPrerequisites()));
        }
        if (request.getIsFeatured() != null) {
            content.setIsFeatured(request.getIsFeatured());
        }

        EducationalContent savedContent = contentRepository.save(content);
        logger.info("Educational content created successfully with ID: {}", savedContent.getContentId());

        return convertToResponseDTO(savedContent);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EducationalContentResponseDTO> getContentById(Long contentId) {
        logger.debug("Fetching educational content by ID: {}", contentId);

        return contentRepository.findById(contentId)
                .map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EducationalContentResponseDTO> getContentByIdWithProgress(Long contentId, Long userId) {
        logger.debug("Fetching educational content by ID: {} with progress for user: {}", contentId, userId);

        return contentRepository.findById(contentId)
                .map(content -> convertToResponseDTOWithProgress(content, userId));
    }

    @Override
    public EducationalContentResponseDTO updateContent(Long contentId, UpdateEducationalContentRequestDTO request) {
        logger.info("Updating educational content ID: {}", contentId);

        EducationalContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ContentNotFoundException("Educational content not found with ID: " + contentId));

        // Update fields if provided
        if (request.getTitle() != null) {
            content.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            content.setDescription(request.getDescription());
        }
        if (request.getContentUrl() != null) {
            content.setContentUrl(request.getContentUrl());
        }
        if (request.getDurationMinutes() != null) {
            content.setDurationMinutes(request.getDurationMinutes());
        }
        if (request.getAuthor() != null) {
            content.setAuthor(request.getAuthor());
        }
        if (request.getSource() != null) {
            content.setSource(request.getSource());
        }
        if (request.getTags() != null) {
            content.setTags(convertListToJson(request.getTags()));
        }
        if (request.getPrerequisites() != null) {
            content.setPrerequisites(convertListToJson(request.getPrerequisites()));
        }
        if (request.getIsFeatured() != null) {
            content.setIsFeatured(request.getIsFeatured());
        }
        if (request.getIsActive() != null) {
            content.setIsActive(request.getIsActive());
        }

        EducationalContent updatedContent = contentRepository.save(content);
        logger.info("Educational content updated successfully: {}", contentId);

        return convertToResponseDTO(updatedContent);
    }

    @Override
    public void deleteContent(Long contentId) {
        logger.info("Deleting educational content ID: {}", contentId);

        EducationalContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ContentNotFoundException("Educational content not found with ID: " + contentId));

        contentRepository.delete(content);
        logger.info("Educational content deleted successfully: {}", contentId);
    }

    @Override
    public EducationalContentResponseDTO updateContentStatus(Long contentId, boolean isActive) {
        logger.info("Updating content status - ID: {}, Active: {}", contentId, isActive);

        EducationalContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ContentNotFoundException("Educational content not found with ID: " + contentId));

        content.setIsActive(isActive);
        EducationalContent updatedContent = contentRepository.save(content);

        return convertToResponseDTO(updatedContent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EducationalContentResponseDTO> getAllActiveContent() {
        logger.debug("Fetching all active educational content");

        return contentRepository.findAllActive()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EducationalContentResponseDTO> getAllActiveContentWithProgress(Long userId) {
        logger.debug("Fetching all active educational content with progress for user: {}", userId);

        return contentRepository.findAllActive()
                .stream()
                .map(content -> convertToResponseDTOWithProgress(content, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EducationalContentResponseDTO> getContentByCategory(ContentCategory category) {
        logger.debug("Fetching educational content by category: {}", category);

        return contentRepository.findByCategory(category.name())
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EducationalContentResponseDTO> getContentByCategoryWithProgress(ContentCategory category, Long userId) {
        logger.debug("Fetching educational content by category: {} with progress for user: {}", category, userId);

        return contentRepository.findByCategory(category.name())
                .stream()
                .map(content -> convertToResponseDTOWithProgress(content, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EducationalContentResponseDTO> getContentByDifficultyLevel(DifficultyLevel difficultyLevel) {
        logger.debug("Fetching educational content by difficulty level: {}", difficultyLevel);

        return contentRepository.findByDifficultyLevel(difficultyLevel.name())
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EducationalContentResponseDTO> getContentByType(ContentType contentType) {
        logger.debug("Fetching educational content by type: {}", contentType);

        return contentRepository.findByContentType(contentType.name())
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EducationalContentResponseDTO> getFeaturedContent() {
        logger.debug("Fetching featured educational content");

        return contentRepository.findFeaturedContent()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EducationalContentResponseDTO> getFeaturedContentWithProgress(Long userId) {
        logger.debug("Fetching featured educational content with progress for user: {}", userId);

        return contentRepository.findFeaturedContent()
                .stream()
                .map(content -> convertToResponseDTOWithProgress(content, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EducationalContentResponseDTO> searchContentByTitle(String title) {
        logger.debug("Searching educational content by title: {}", title);

        return contentRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EducationalContentResponseDTO> searchContentByAuthor(String author) {
        logger.debug("Searching educational content by author: {}", author);

        return contentRepository.findByAuthorContainingIgnoreCase(author)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EducationalContentResponseDTO> getContentByCategoryAndDifficulty(ContentCategory category, DifficultyLevel difficultyLevel) {
        logger.debug("Fetching educational content by category: {} and difficulty: {}", category, difficultyLevel);

        return contentRepository.findByCategoryAndDifficultyLevel(category.name(), difficultyLevel.name())
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EducationalContentResponseDTO> getContentCreatedAfter(LocalDateTime date) {
        logger.debug("Fetching educational content created after: {}", date);

        return contentRepository.findCreatedAfter(date)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EducationalContentResponseDTO> getContentByMinimumRating(Double minRating) {
        logger.debug("Fetching educational content with minimum rating: {}", minRating);

        return contentRepository.findByMinimumRating(minRating)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EducationalContentResponseDTO> getMostPopularContent(int limit) {
        logger.debug("Fetching most popular educational content, limit: {}", limit);

        return contentRepository.findMostPopular(limit)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EducationalContentResponseDTO> getContentByDurationRange(Integer minDuration, Integer maxDuration) {
        logger.debug("Fetching educational content by duration range: {} - {}", minDuration, maxDuration);

        return contentRepository.findByDurationRange(minDuration, maxDuration)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EducationalContentResponseDTO> searchContentByTag(String tag) {
        logger.debug("Searching educational content by tag: {}", tag);

        return contentRepository.findByTagsContaining(tag)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void incrementViewCount(Long contentId) {
        logger.debug("Incrementing view count for content ID: {}", contentId);

        contentRepository.incrementViewCount(contentId);
    }

    @Override
    public EducationalContentResponseDTO updateContentRating(Long contentId, Double rating) {
        logger.info("Updating rating for content ID: {}, new rating: {}", contentId, rating);

        EducationalContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ContentNotFoundException("Educational content not found with ID: " + contentId));

        content.setRating(rating);
        EducationalContent updatedContent = contentRepository.save(content);

        return convertToResponseDTO(updatedContent);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getContentStatistics() {
        logger.debug("Fetching content statistics");

        Long totalActive = getTotalActiveContentCount();
        Double averageRating = getAverageContentRating();
        Map<ContentCategory, Long> statsByCategory = getContentStatsByCategory();

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalActiveContent", totalActive);
        statistics.put("averageRating", averageRating != null ? averageRating : 0.0);
        statistics.put("contentByCategory", statsByCategory);

        return statistics;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<ContentCategory, Long> getContentStatsByCategory() {
        Map<ContentCategory, Long> stats = new HashMap<>();
        for (ContentCategory category : ContentCategory.values()) {
            Long count = contentRepository.countByCategory(category.name());
            stats.put(category, count);
        }
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalActiveContentCount() {
        return contentRepository.countActiveContent();
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageContentRating() {
        Double average = contentRepository.getAverageRating();
        return average != null ? average : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateContentExists(Long contentId) {
        return contentRepository.findById(contentId)
                .map(EducationalContent::getIsActive)
                .orElse(false);
    }

    // Private helper methods
    private EducationalContentResponseDTO convertToResponseDTO(EducationalContent content) {
        EducationalContentResponseDTO dto = new EducationalContentResponseDTO();
        dto.setContentId(content.getContentId());
        dto.setTitle(content.getTitle());
        dto.setDescription(content.getDescription());
        dto.setCategory(content.getCategory().toString());
        dto.setDifficultyLevel(content.getDifficultyLevel().toString());
        dto.setContentType(content.getContentType().toString());
        dto.setContentUrl(content.getContentUrl());
        dto.setDurationMinutes(content.getDurationMinutes());
        dto.setAuthor(content.getAuthor());
        dto.setSource(content.getSource());
        dto.setTags(convertJsonToList(content.getTags(), String.class));
        dto.setPrerequisites(convertJsonToList(content.getPrerequisites(), Long.class));
        dto.setIsFeatured(content.getIsFeatured());
        dto.setViewCount(content.getViewCount());
        dto.setRating(content.getRating());
        dto.setCreatedAt(content.getCreatedAt());
        dto.setUpdatedAt(content.getUpdatedAt());

        return dto;
    }

    private EducationalContentResponseDTO convertToResponseDTOWithProgress(EducationalContent content, Long userId) {
        EducationalContentResponseDTO dto = convertToResponseDTO(content);

        // Get user progress if userId is provided
        if (userId != null) {
            Optional<UserProgressDTO> progress = userProgressService.getUserProgress(userId, content.getContentId());
            progress.ifPresent(dto::setUserProgress);
        }

        return dto;
    }

    private String convertListToJson(List<?> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            logger.error("Error converting list to JSON", e);
            return "[]";
        }
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> convertJsonToList(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            if (clazz == String.class) {
                return (List<T>) objectMapper.readValue(json, new TypeReference<List<String>>() {});
            } else if (clazz == Long.class) {
                return (List<T>) objectMapper.readValue(json, new TypeReference<List<Long>>() {});
            }
            return new ArrayList<>();
        } catch (JsonProcessingException e) {
            logger.error("Error converting JSON to list", e);
            return new ArrayList<>();
        }
    }
}