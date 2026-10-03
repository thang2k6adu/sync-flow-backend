package com.kruzetech.vocab.controller;

import com.kruzetech.vocab.controller.dto.CreateDeckRequest;
import com.kruzetech.vocab.controller.dto.DeckDto;
import com.kruzetech.vocab.security.AuthUser;
import com.kruzetech.vocab.service.DeckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "decks", description = "Quản lý bộ từ vựng")
@RestController
@RequestMapping("/decks")
@RequiredArgsConstructor
public class DeckController {

    private final DeckService deckService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Tạo bộ từ vựng mới")
    public DeckDto create(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody CreateDeckRequest request) {
        return deckService.createDeck(user.id(), request);
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách các bộ từ vựng của người dùng")
    public List<DeckDto> list(@AuthenticationPrincipal AuthUser user) {
        return deckService.listDecks(user.id());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin chi tiết một bộ từ vựng")
    public DeckDto get(@AuthenticationPrincipal AuthUser user, @PathVariable String id) {
        return deckService.getDeck(id, user.id());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa một bộ từ vựng")
    public Map<String, String> delete(@AuthenticationPrincipal AuthUser user, @PathVariable String id) {
        deckService.deleteDeck(id, user.id());
        return Map.of("message", "Deck deleted successfully");
    }
}
