package com.kruzetech.vocab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VocabFlowTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Value("${app.jwt.secret}") String secret;

    private String token(String userId, String role) {
        return Jwts.builder()
                .subject(userId)
                .claim("email", userId + "@example.com")
                .claim("role", role)
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    private JsonNode call(MockHttpServletRequestBuilder req, String token, String body, int expected) throws Exception {
        if (token != null) {
            req.header("Authorization", "Bearer " + token);
        }
        if (body != null) {
            req.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        String out = mvc.perform(req).andExpect(status().is(expected)).andReturn().getResponse().getContentAsString();
        return mapper.readTree(out);
    }

    @Test
    void healthCheckIsPublic() throws Exception {
        mvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ok"));
    }

    @Test
    void unauthorizedAccessReturns401() throws Exception {
        call(get("/decks"), null, null, 401);
    }

    @Test
    void deckAndCardFlowScopedToOwner() throws Exception {
        String alice = token("alice", "USER");
        String bob = token("bob", "USER");

        // 1. Alice creates a deck
        String createDeckBody = "{\"name\":\"IELTS Vocabulary\",\"description\":\"Band 7.0+\"}";
        JsonNode deckRes = call(post("/decks"), alice, createDeckBody, 201);
        String deckId = deckRes.at("/data/id").asText();
        assertEquals("IELTS Vocabulary", deckRes.at("/data/name").asText());
        assertFalse(deckId.isEmpty());

        // 2. Alice lists decks
        JsonNode listRes = call(get("/decks"), alice, null, 200);
        assertEquals(1, listRes.at("/data").size());

        // 3. Alice creates a card with meanings and exercises
        String createCardBody = "{\n"
                + "  \"deckId\": \"" + deckId + "\",\n"
                + "  \"term\": \"ubiquitous\",\n"
                + "  \"phonetic\": \"/juːˈbɪk.wə.təs/\",\n"
                + "  \"meanings\": [\n"
                + "    {\"order\": 1, \"pos\": \"adj\", \"definition_en\": \"present everywhere\", \"meaning_vi\": \"phổ biến\"}\n"
                + "  ],\n"
                + "  \"exercises\": [\n"
                + "    {\n"
                + "      \"exerciseType\": \"FILL_BLANK\",\n"
                + "      \"targetSentence\": \"Smartphones have become ubiquitous in modern society.\",\n"
                + "      \"vietnameseTranslation\": \"Điện thoại thông minh đã trở nên phổ biến ở khắp nơi trong xã hội hiện đại.\",\n"
                + "      \"tokens\": [\"Smartphones\", \"have\", \"become\", \"ubiquitous\", \"in\", \"modern\", \"society.\"],\n"
                + "      \"targetIndex\": 3\n"
                + "    }\n"
                + "  ]\n"
                + "}";
        JsonNode cardRes = call(post("/cards"), alice, createCardBody, 201);
        String cardId = cardRes.at("/data/id").asText();
        assertEquals("ubiquitous", cardRes.at("/data/term").asText());

        // 4. Alice gets cards by deck
        JsonNode cardsByDeck = call(get("/cards").param("deckId", deckId), alice, null, 200);
        assertEquals(1, cardsByDeck.at("/data").size());

        // 5. Get card detail
        JsonNode cardDetail = call(get("/cards/" + cardId), alice, null, 200);
        assertEquals("ubiquitous", cardDetail.at("/data/term").asText());

        // 6. Study queue check
        JsonNode queueRes = call(get("/study/queue").param("deckId", deckId), alice, null, 200);
        assertNotNull(queueRes.at("/data/items"));

        // 7. Bob cannot access Alice's deck
        call(get("/decks/" + deckId), bob, null, 404);
        call(delete("/decks/" + deckId), bob, null, 404);

        // 8. Alice deletes deck
        call(delete("/decks/" + deckId), alice, null, 200);
        call(get("/decks/" + deckId), alice, null, 404);
    }
}
