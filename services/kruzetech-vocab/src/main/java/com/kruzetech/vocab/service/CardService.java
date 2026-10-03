package com.kruzetech.vocab.service;

import com.kruzetech.vocab.controller.dto.CardDto;
import com.kruzetech.vocab.controller.dto.CardExerciseDto;
import com.kruzetech.vocab.controller.dto.CreateCardRequest;
import com.kruzetech.vocab.controller.dto.CreateExerciseRequest;
import com.kruzetech.vocab.core.exception.ApiException;
import com.kruzetech.vocab.entity.CardExercise;
import com.kruzetech.vocab.entity.Deck;
import com.kruzetech.vocab.entity.UserCardProgress;
import com.kruzetech.vocab.entity.UserCardProgressId;
import com.kruzetech.vocab.entity.VocabCard;
import com.kruzetech.vocab.repository.CardExerciseRepository;
import com.kruzetech.vocab.repository.DeckRepository;
import com.kruzetech.vocab.repository.UserCardProgressRepository;
import com.kruzetech.vocab.repository.VocabCardRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardService {

    private final VocabCardRepository cardRepository;
    private final CardExerciseRepository exerciseRepository;
    private final DeckRepository deckRepository;
    private final UserCardProgressRepository progressRepository;

    @Transactional
    public CardDto createCard(String userId, CreateCardRequest request) {
        Deck deck = deckRepository.findByIdAndUserId(request.deckId(), userId)
                .orElseThrow(() -> ApiException.notFound("Deck not found: " + request.deckId()));

        VocabCard card = VocabCard.builder()
                .deckId(deck.getId())
                .term(request.term().trim())
                .phonetic(request.phonetic())
                .audioUrl(request.audioUrl())
                .meanings(request.meanings())
                .collocations(request.collocations() != null ? request.collocations() : List.of())
                .build();
        VocabCard savedCard = cardRepository.save(card);

        List<CardExercise> savedExercises = new ArrayList<>();
        if (request.exercises() != null && !request.exercises().isEmpty()) {
            for (CreateExerciseRequest er : request.exercises()) {
                CardExercise exercise = CardExercise.builder()
                        .cardId(savedCard.getId())
                        .exerciseType(er.exerciseType())
                        .meaningHint(er.meaningHint())
                        .targetSentence(er.targetSentence())
                        .vietnameseTranslation(er.vietnameseTranslation())
                        .tokens(er.tokens() != null ? er.tokens() : List.of())
                        .distractorTokens(er.distractorTokens() != null ? er.distractorTokens() : List.of())
                        .targetIndex(er.targetIndex())
                        .audioUrl(er.audioUrl())
                        .build();
                savedExercises.add(exerciseRepository.save(exercise));
            }
        }

        // Khởi tạo tiến trình SRS ban đầu cho người tạo thẻ
        UserCardProgress initialProgress = UserCardProgress.builder()
                .id(new UserCardProgressId(userId, savedCard.getId()))
                .state("new")
                .masteryLevel(1)
                .easeFactor(new BigDecimal("2.50"))
                .intervalDays(0)
                .repetitionCount(0)
                .lastExerciseIndex(0)
                .dueDate(Instant.now())
                .lapsesCount(0)
                .build();
        progressRepository.save(initialProgress);

        return toCardDto(savedCard, savedExercises);
    }

    @Transactional(readOnly = true)
    public List<CardDto> listCardsByDeck(String deckId, String userId) {
        deckRepository.findByIdAndUserId(deckId, userId)
                .orElseThrow(() -> ApiException.notFound("Deck not found: " + deckId));

        List<VocabCard> cards = cardRepository.findByDeckIdOrderByCreatedAtAsc(deckId);
        List<String> cardIds = cards.stream().map(VocabCard::getId).toList();
        List<CardExercise> allExercises = exerciseRepository.findByCardIdIn(cardIds);

        return cards.stream().map(c -> {
            List<CardExercise> cardExercises = allExercises.stream()
                    .filter(e -> e.getCardId().equals(c.getId()))
                    .toList();
            return toCardDto(c, cardExercises);
        }).toList();
    }

    @Transactional(readOnly = true)
    public CardDto getCard(String cardId) {
        VocabCard card = cardRepository.findById(cardId)
                .orElseThrow(() -> ApiException.notFound("Card not found: " + cardId));
        List<CardExercise> exercises = exerciseRepository.findByCardIdOrderByTargetIndexAsc(cardId);
        return toCardDto(card, exercises);
    }

    private CardDto toCardDto(VocabCard card, List<CardExercise> exercises) {
        List<CardExerciseDto> exerciseDtos = exercises.stream()
                .map(this::toExerciseDto)
                .toList();

        return new CardDto(
                card.getId(),
                card.getDeckId(),
                card.getTerm(),
                card.getPhonetic(),
                card.getAudioUrl(),
                card.getMeanings(),
                card.getCollocations(),
                exerciseDtos);
    }

    public CardExerciseDto toExerciseDto(CardExercise e) {
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
