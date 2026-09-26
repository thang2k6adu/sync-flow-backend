package com.kruzetech.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayRoutingTest {

    static MockWebServer auth = new MockWebServer();
    static MockWebServer task = new MockWebServer();

    @LocalServerPort int port;

    @BeforeAll
    static void start() throws Exception {
        auth.start();
        task.start();
    }

    @AfterAll
    static void stop() throws Exception {
        auth.shutdown();
        task.shutdown();
    }

    @DynamicPropertySource
    static void routes(DynamicPropertyRegistry r) {
        r.add("AUTH_SERVICE_URL", () -> "http://localhost:" + auth.getPort());
        r.add("TASK_SERVICE_URL", () -> "http://localhost:" + task.getPort());
    }

    private WebTestClient client() {
        return WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Test
    void routesByPathKeepingPrefixAndForwardsHeaders() throws Exception {
        task.enqueue(new MockResponse().setBody("{\"from\":\"task\"}").addHeader("Content-Type", "application/json"));
        auth.enqueue(new MockResponse().setBody("{\"from\":\"auth\"}").addHeader("Content-Type", "application/json"));

        client().get().uri("/api/tasks?page=2").header("Authorization", "Bearer abc").exchange()
                .expectStatus().isOk()
                .expectHeader().exists("X-Trace-Id")
                .expectBody().jsonPath("$.from").isEqualTo("task");
        RecordedRequest t = task.takeRequest();
        assertEquals("/api/tasks?page=2", t.getPath());
        assertEquals("Bearer abc", t.getHeader("Authorization"));
        assertNotNull(t.getHeader("X-Trace-Id"));

        client().post().uri("/api/auth/login").contentType(MediaType.APPLICATION_JSON).bodyValue("{}").exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.from").isEqualTo("auth");
        assertEquals("/api/auth/login", auth.takeRequest().getPath());
    }

    @Test
    void clientTraceIdIsPreserved() throws Exception {
        task.enqueue(new MockResponse().setBody("{}").addHeader("Content-Type", "application/json"));
        client().get().uri("/api/tasks").header("X-Trace-Id", "trace-123").exchange()
                .expectHeader().valueEquals("X-Trace-Id", "trace-123");
        assertEquals("trace-123", task.takeRequest().getHeader("X-Trace-Id"));
    }

    @Test
    void unknownRouteReturnsEnvelope404() {
        client().get().uri("/api/nothing").exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo(true)
                .jsonPath("$.code").isEqualTo(404)
                .jsonPath("$.traceId").isNotEmpty();
    }
}
