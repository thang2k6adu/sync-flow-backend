package com.kruzetech.vocab.controller;

import com.kruzetech.vocab.controller.dto.CardDto;
import com.kruzetech.vocab.controller.dto.CreateCardRequest;
import com.kruzetech.vocab.security.AuthUser;
import com.kruzetech.vocab.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "cards", description = "Quản lý thẻ từ vựng & bài tập câu")
@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Tạo thẻ từ vựng mới (kèm đa nghĩa JSONB và danh sách bài tập câu)")
    public CardDto create(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody CreateCardRequest request) {
        return cardService.createCard(user.id(), request);
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách thẻ từ vựng theo bộ thẻ (Deck)")
    public List<CardDto> listByDeck(@AuthenticationPrincipal AuthUser user, @RequestParam String deckId) {
        return cardService.listCardsByDeck(deckId, user.id());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết một thẻ từ vựng kèm toàn bộ bài tập câu")
    public CardDto get(@PathVariable String id) {
        return cardService.getCard(id);
    }
}
