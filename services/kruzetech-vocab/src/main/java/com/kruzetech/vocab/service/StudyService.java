package com.kruzetech.vocab.service;

import com.kruzetech.vocab.controller.dto.CardExerciseDto;
import com.kruzetech.vocab.controller.dto.StudyQueueItemDto;
import com.kruzetech.vocab.controller.dto.StudyQueueResponse;
import com.kruzetech.vocab.controller.dto.StudySubmitResponse;
import com.kruzetech.vocab.controller.dto.SubmitStudyRequest;
import com.kruzetech.vocab.core.exception.ApiException;
import com.kruzetech.vocab.entity.CardExercise;
import com.kruzetech.vocab.entity.ReviewLog;
import com.kruzetech.vocab.entity.UserCardProgress;
import com.kruzetech.vocab.entity.UserCardProgressId;
import com.kruzetech.vocab.entity.VocabCard;
import com.kruzetech.vocab.repository.CardExerciseRepository;
import com.kruzetech.vocab.repository.ReviewLogRepository;
import com.kruzetech.vocab.repository.UserCardProgressRepository;
import com.kruzetech.vocab.repository.VocabCardRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudyService {

    private final UserCardProgressRepository progressRepository;
    private final VocabCardRepository cardRepository;
    private final CardExerciseRepository exerciseRepository;
    private final ReviewLogRepository reviewLogRepository;
    private final SrsEngine srsEngine;

    @Transactional(readOnly = true)
    public StudyQueueResponse getReviewQueue(String userId, int limit, int page) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        int safePage = Math.max(page, 0);
        Instant now = Instant.now();
        PageRequest pageRequest = PageRequest.of(safePage, safeLimit);

        List<UserCardProgress> dueProgressList = progressRepository.findDueCards(userId, now, pageRequest);
        long totalDue = progressRepository.countDueCards(userId, now);

        return buildQueueResponse(dueProgressList, totalDue);
    }

    @Transactional(readOnly = true)
    public StudyQueueResponse getCramQueue(String userId, String deckId, int limit, int page) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        int safePage = Math.max(page, 0);
        Instant now = Instant.now();
        PageRequest pageRequest = PageRequest.of(safePage, safeLimit);

        List<VocabCard> deckCards = cardRepository.findCardsByDeckPaginated(deckId, pageRequest);
        long totalDue = cardRepository.countCardsByDeck(deckId);

        List<UserCardProgress> dueProgressList = new ArrayList<>();
        for (VocabCard card : deckCards) {
            UserCardProgress progress = progressRepository.findByIdUserIdAndIdCardId(userId, card.getId())
                    .orElseGet(() -> UserCardProgress.builder()
                            .id(new UserCardProgressId(userId, card.getId()))
                            .state("new")
                            .masteryLevel(1)
                            .easeFactor(new BigDecimal("2.50"))
                            .intervalDays(0)
                            .repetitionCount(0)
                            .lastExerciseIndex(0)
                            .dueDate(now)
                            .lapsesCount(0)
                            .build());
            dueProgressList.add(progress);
        }

        return buildQueueResponse(dueProgressList, totalDue);
    }

    private StudyQueueResponse buildQueueResponse(List<UserCardProgress> dueProgressList, long totalDue) {
        if (dueProgressList.isEmpty()) {
            return new StudyQueueResponse(0, List.of());
        }

        List<String> cardIds = dueProgressList.stream().map(p -> p.getId().getCardId()).toList();
        Map<String, VocabCard> cardMap = cardRepository.findByIdIn(cardIds).stream()
                .collect(Collectors.toMap(VocabCard::getId, c -> c));

        List<CardExercise> allExercises = exerciseRepository.findByCardIdIn(cardIds);
        Map<String, List<CardExercise>> exerciseMap = allExercises.stream()
                .collect(Collectors.groupingBy(CardExercise::getCardId));

        List<StudyQueueItemDto> queueItems = new ArrayList<>();
        for (UserCardProgress progress : dueProgressList) {
            String cardId = progress.getId().getCardId();
            VocabCard card = cardMap.get(cardId);
            if (card == null) continue;

            List<CardExercise> exercises = exerciseMap.getOrDefault(cardId, List.of());
            CardExercise selectedExercise = null;

            if (!exercises.isEmpty()) {
                // Lọc bài tập theo masteryLevel: Level 2 chỉ được dùng sentence_builder
                List<CardExercise> validExercises = exercises;
                if (progress.getMasteryLevel() == 2) {
                    List<CardExercise> filtered = exercises.stream()
                            .filter(e -> "sentence_builder".equals(e.getExerciseType()))
                            .toList();
                    if (!filtered.isEmpty()) {
                        validExercises = filtered;
                    }
                }

                // Thuật toán Round-Robin phân bổ bài tập
                int selectedIndex = (progress.getLastExerciseIndex() + 1) % validExercises.size();
                selectedExercise = validExercises.get(selectedIndex);
            }

            CardExerciseDto exerciseDto = selectedExercise != null ? toExerciseDto(selectedExercise) : null;
            boolean isLeech = progress.getLapsesCount() >= 5;

            queueItems.add(new StudyQueueItemDto(
                    card.getId(),
                    card.getTerm(),
                    card.getPhonetic(),
                    card.getAudioUrl(),
                    progress.getMasteryLevel(),
                    progress.getState(),
                    progress.getLapsesCount(),
                    isLeech,
                    card.getMeanings(),
                    exerciseDto));
        }

        return new StudyQueueResponse(totalDue, queueItems);
    }

    @Transactional
    public StudySubmitResponse submitStudy(String userId, SubmitStudyRequest request) {
        String cardId = request.cardId();
        UserCardProgress progress = progressRepository.findByIdUserIdAndIdCardId(userId, cardId)
                .orElseGet(() -> {
                    // Nếu thẻ chưa có bản ghi tiến trình, tự động tạo mới
                    return UserCardProgress.builder()
                            .id(new UserCardProgressId(userId, cardId))
                            .state("new")
                            .masteryLevel(1)
                            .easeFactor(new BigDecimal("2.50"))
                            .intervalDays(0)
                            .repetitionCount(0)
                            .lastExerciseIndex(0)
                            .dueDate(Instant.now())
                            .lapsesCount(0)
                            .build();
                });

        int prevLevel = progress.getMasteryLevel();
        int prevInterval = progress.getIntervalDays();

        SrsEngine.Rating manualRating = SrsEngine.Rating.fromString(request.manualRating());
        SrsEngine.Telemetry telemetry = SrsEngine.Telemetry.builder()
                .timeSpentMs(request.timeSpentMs())
                .mistakesCount(request.mistakesCount())
                .usedHint(request.usedHint())
                .manualRating(manualRating)
                .build();

        SrsEngine.SrsResult result = srsEngine.calculateNextState(progress, telemetry);

        // Cập nhật câu bài tập index tiếp theo
        List<CardExercise> exercises = exerciseRepository.findByCardIdOrderByTargetIndexAsc(cardId);
        int nextExIdx = progress.getLastExerciseIndex();
        if (!exercises.isEmpty()) {
            nextExIdx = (progress.getLastExerciseIndex() + 1) % exercises.size();
        }

        boolean isNewCard = "new".equals(progress.getState());

        if (!request.isCram() || isNewCard) {
            // Xác định state mới
            String newState = "review";
            if (result.getNewLevel() >= 3 && result.getNewIntervalDays() >= 21) {
                newState = "mastered";
            } else if (result.getNewRepetitionCount() == 0) {
                newState = "learning";
            }

            progress.setState(newState);
            progress.setMasteryLevel(result.getNewLevel());
            progress.setEaseFactor(result.getNewEaseFactor());
            progress.setIntervalDays(result.getNewIntervalDays());
            progress.setRepetitionCount(result.getNewRepetitionCount());
            progress.setLapsesCount(result.getNewLapsesCount());
            progress.setDueDate(result.getNewDueDate());
            progress.setLastReviewedAt(Instant.now());
        }

        progress.setLastExerciseIndex(nextExIdx);

        progressRepository.save(progress);

        // Ghi nhật ký ôn tập (review_logs)
        ReviewLog log = ReviewLog.builder()
                .userId(userId)
                .cardId(cardId)
                .rating(result.getRating().name().toLowerCase())
                .intervalBefore(prevInterval)
                .intervalAfter(result.getNewIntervalDays())
                .timeSpentMs(request.timeSpentMs())
                .mistakesCount(request.mistakesCount())
                .usedHint(request.usedHint())
                .build();
        reviewLogRepository.save(log);

        return new StudySubmitResponse(
                cardId,
                result.getRating().name().toLowerCase(),
                prevLevel,
                result.getNewLevel(),
                prevInterval,
                result.getNewIntervalDays(),
                result.getNewEaseFactor(),
                result.getNewDueDate(),
                result.isLeech());
    }

    private CardExerciseDto toExerciseDto(CardExercise e) {
        return new CardExerciseDto(
                e.getId(),
                e.getCardId(),
                e.getExerciseType(),
                e.getMeaningHint(),
                e.getTargetSentence(),
                e.getVietnameseTranslation(),
                e.getTokens(),
                e.getDistractorTokens(),
                e.getTargetIndex(),
                e.getAudioUrl());
    }
}
