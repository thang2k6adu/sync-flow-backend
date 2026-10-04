package com.kruzetech.vocab.service;

import com.kruzetech.vocab.controller.dto.CreateDeckRequest;
import com.kruzetech.vocab.controller.dto.DeckDto;
import com.kruzetech.vocab.core.exception.ApiException;
import com.kruzetech.vocab.entity.Deck;
import com.kruzetech.vocab.repository.DeckRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeckService {

    private final DeckRepository deckRepository;

    @Transactional
    public DeckDto createDeck(String userId, CreateDeckRequest request) {
        Deck deck = Deck.builder()
                .userId(userId)
                .name(request.name())
                .description(request.description())
                .category(request.category())
                .iconUrl(request.iconUrl())
                .cefrLevel(request.cefrLevel())
                .build();
        Deck saved = deckRepository.save(deck);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<DeckDto> listDecks(String userId) {
        return deckRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public DeckDto getDeck(String id, String userId) {
        Deck deck = deckRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> ApiException.notFound("Deck not found: " + id));
        return toDto(deck);
    }

    @Transactional
    public void deleteDeck(String id, String userId) {
        Deck deck = deckRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> ApiException.notFound("Deck not found: " + id));
        deckRepository.delete(deck);
    }

    private DeckDto toDto(Deck deck) {
        return new DeckDto(
                deck.getId(),
                deck.getUserId(),
                deck.getName(),
                deck.getDescription(),
                deck.getCategory(),
                deck.getIconUrl(),
                deck.getCefrLevel(),
                deck.getCreatedAt());
    }
}
