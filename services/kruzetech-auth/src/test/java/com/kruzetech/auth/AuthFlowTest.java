package com.kruzetech.auth;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kruzetech.auth.core.exception.ApiException;
import com.kruzetech.auth.service.FirebaseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @MockBean FirebaseService firebase;

    private JsonNode call(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder req, int expected)
            throws Exception {
        String body = mvc.perform(req).andExpect(status().is(expected)).andReturn().getResponse().getContentAsString();
        return mapper.readTree(body);
    }

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder json(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder b, String body) {
        return b.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    @Test
    void registerLoginProfileRefreshLogout() throws Exception {
        JsonNode reg = call(
                json(post("/auth/register"), "{\"email\":\"a@example.com\",\"password\":\"secret1\",\"firstName\":\"A\"}"),
                201);
        org.junit.jupiter.api.Assertions.assertFalse(reg.get("error").asBoolean());
        org.junit.jupiter.api.Assertions.assertEquals(7200, reg.at("/data/tokens/expiresIn").asInt());
        org.junit.jupiter.api.Assertions.assertEquals("USER", reg.at("/data/user/role").asText());

        call(json(post("/auth/register"), "{\"email\":\"a@example.com\",\"password\":\"secret1\"}"), 409);

        JsonNode login = call(json(post("/auth/login"), "{\"email\":\"a@example.com\",\"password\":\"secret1\"}"), 200);
        String access = login.at("/data/tokens/accessToken").asText();
        String refresh = login.at("/data/tokens/refreshToken").asText();

        call(json(post("/auth/login"), "{\"email\":\"a@example.com\",\"password\":\"wrong-pass\"}"), 401);

        mvc.perform(get("/users/profile").header("Authorization", "Bearer " + access))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("a@example.com"));

        // token cũ/rác gửi kèm endpoint public không được làm hỏng request (Flutter gửi "Bearer null")
        call(json(post("/auth/login").header("Authorization", "Bearer null"),
                "{\"email\":\"a@example.com\",\"password\":\"secret1\"}"), 200);

        JsonNode refreshed = call(json(post("/auth/refresh"), "{\"refreshToken\":\"" + refresh + "\"}"), 200);
        String newRefresh = refreshed.at("/data/refreshToken").asText();
        org.junit.jupiter.api.Assertions.assertNotEquals(refresh, newRefresh);

        // refresh token cũ đã bị thu hồi
        call(json(post("/auth/refresh"), "{\"refreshToken\":\"" + refresh + "\"}"), 401);

        mvc.perform(post("/auth/logout").header("Authorization", "Bearer " + access))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("Logged out successfully"));
        call(json(post("/auth/refresh"), "{\"refreshToken\":\"" + newRefresh + "\"}"), 401);
    }

    @Test
    void firebaseLoginCreatesThenReusesUser() throws Exception {
        when(firebase.verifyIdToken("good"))
                .thenReturn(new FirebaseService.Identity("uid-1", "fb@example.com", "Nguyen Van A", "https://a/p.png"));
        when(firebase.verifyIdToken("expired")).thenThrow(ApiException.unauthorized("Firebase token expired"));

        JsonNode first = call(json(post("/auth/firebase/login"), "{\"idToken\":\"good\",\"platform\":\"android\"}"), 200);
        org.junit.jupiter.api.Assertions.assertEquals("Nguyen", first.at("/data/user/firstName").asText());
        org.junit.jupiter.api.Assertions.assertEquals("Van A", first.at("/data/user/lastName").asText());
        String id = first.at("/data/user/id").asText();

        JsonNode second = call(json(post("/auth/firebase/login"), "{\"idToken\":\"good\"}"), 200);
        org.junit.jupiter.api.Assertions.assertEquals(id, second.at("/data/user/id").asText());

        JsonNode expired = call(json(post("/auth/firebase/login"), "{\"idToken\":\"expired\"}"), 401);
        org.junit.jupiter.api.Assertions.assertEquals("Firebase token expired", expired.get("message").asText());
        org.junit.jupiter.api.Assertions.assertTrue(expired.get("error").asBoolean());
    }

    @Test
    void validationAndAuthorizationErrorsUseEnvelope() throws Exception {
        JsonNode bad = call(json(post("/auth/register"), "{\"email\":\"nope\",\"password\":\"1\"}"), 400);
        org.junit.jupiter.api.Assertions.assertTrue(bad.get("error").asBoolean());
        org.junit.jupiter.api.Assertions.assertEquals(400, bad.get("code").asInt());
        org.junit.jupiter.api.Assertions.assertTrue(bad.get("data").isNull());
        org.junit.jupiter.api.Assertions.assertFalse(bad.get("traceId").asText().isEmpty());

        // field lạ bị từ chối như forbidNonWhitelisted của Nest
        call(json(post("/auth/login"), "{\"email\":\"a@example.com\",\"password\":\"x\",\"extra\":1}"), 400);

        JsonNode noToken = call(get("/users/profile"), 401);
        org.junit.jupiter.api.Assertions.assertEquals(401, noToken.get("code").asInt());

        // USER thường không được liệt kê / xoá user
        JsonNode reg = call(json(post("/auth/register"), "{\"email\":\"u@example.com\",\"password\":\"secret1\"}"), 201);
        String access = reg.at("/data/tokens/accessToken").asText();
        mvc.perform(get("/users").header("Authorization", "Bearer " + access)).andExpect(status().isForbidden());
        mvc.perform(delete("/users/abc").header("Authorization", "Bearer " + access)).andExpect(status().isForbidden());
    }

    @Test
    void adminCanListAndPaginateUsers() throws Exception {
        JsonNode login = call(json(post("/auth/login"), "{\"email\":\"admin@example.com\",\"password\":\"admin123\"}"), 200);
        String access = login.at("/data/tokens/accessToken").asText();

        mvc.perform(get("/users").param("page", "1").param("limit", "5").param("search", "ADMIN")
                        .header("Authorization", "Bearer " + access))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].email").value("admin@example.com"))
                .andExpect(jsonPath("$.data.meta.currentPage").value(1))
                .andExpect(jsonPath("$.data.meta.itemsPerPage").value(5));

        mvc.perform(get("/users").param("limit", "1000").header("Authorization", "Bearer " + access))
                .andExpect(status().isBadRequest());
    }

    @Test
    void healthIsPublic() throws Exception {
        mvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ok"))
                .andExpect(jsonPath("$.data.database").value("connected"));
    }
}
