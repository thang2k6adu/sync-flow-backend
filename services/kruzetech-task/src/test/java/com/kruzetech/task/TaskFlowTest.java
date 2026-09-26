package com.kruzetech.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
class TaskFlowTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Value("${app.jwt.secret}") String secret;

    /** Giả lập access token do kruzetech-auth cấp: sub, email, role. */
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
    void crudIsScopedToOwnerAndAdminSeesAll() throws Exception {
        String alice = token("alice", "USER");
        String bob = token("bob", "USER");
        String admin = token("root", "ADMIN");

        JsonNode created = call(post("/tasks"), alice, "{\"title\":\"Write docs\",\"description\":\"boilerplate\"}", 201);
        assertEquals("TODO", created.at("/data/status").asText());
        assertEquals("alice", created.at("/data/userId").asText());
        String id = created.at("/data/id").asText();

        call(post("/tasks"), alice, "{\"title\":\"Ship it\",\"status\":\"IN_PROGRESS\"}", 201);
        call(post("/tasks"), bob, "{\"title\":\"Bob task\"}", 201);

        JsonNode mine = call(get("/tasks"), alice, null, 200);
        assertEquals(2, mine.at("/data/meta/totalItems").asInt());

        JsonNode filtered = call(get("/tasks").param("status", "IN_PROGRESS"), alice, null, 200);
        assertEquals(1, filtered.at("/data/meta/totalItems").asInt());
        assertEquals("Ship it", filtered.at("/data/items/0/title").asText());

        JsonNode searched = call(get("/tasks").param("search", "DOCS"), alice, null, 200);
        assertEquals(1, searched.at("/data/meta/totalItems").asInt());

        // bob không thấy / không sửa / không xoá task của alice
        call(get("/tasks/" + id), bob, null, 404);
        call(patch("/tasks/" + id), bob, "{\"status\":\"DONE\"}", 404);
        call(delete("/tasks/" + id), bob, null, 404);

        JsonNode updated = call(patch("/tasks/" + id), alice, "{\"status\":\"DONE\"}", 200);
        assertEquals("DONE", updated.at("/data/status").asText());
        assertEquals("Write docs", updated.at("/data/title").asText());

        assertTrue(call(get("/tasks"), admin, null, 200).at("/data/meta/totalItems").asInt() >= 3);
        call(get("/tasks/" + id), admin, null, 200);

        call(delete("/tasks/" + id), alice, null, 200);
        call(get("/tasks/" + id), alice, null, 404);
    }

    @Test
    void errorsUseEnvelope() throws Exception {
        JsonNode noToken = call(get("/tasks"), null, null, 401);
        assertTrue(noToken.get("error").asBoolean());
        assertEquals(401, noToken.get("code").asInt());

        call(get("/tasks"), "not-a-jwt", null, 401);

        String user = token("carol", "USER");
        JsonNode bad = call(post("/tasks"), user, "{\"title\":\"\"}", 400);
        assertTrue(bad.get("data").isNull());
        call(post("/tasks"), user, "{\"title\":\"x\",\"status\":\"NOPE\"}", 400);
        call(get("/tasks").param("limit", "1000"), user, null, 400);
    }

    @Test
    void healthIsPublic() throws Exception {
        mvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.database").value("connected"));
    }
}
