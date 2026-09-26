package com.kruzetech.auth.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthErrorCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.kruzetech.auth.config.AppProperties;
import com.kruzetech.auth.core.exception.ApiException;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** Verify Firebase ID token bằng Admin SDK. Chỉ khởi tạo khi đủ project-id, client-email, private-key. */
@Service
public class FirebaseService {

    private static final Logger log = LoggerFactory.getLogger(FirebaseService.class);

    public record Identity(String uid, String email, String name, String picture) {}

    private final AppProperties.Firebase config;
    private FirebaseAuth firebaseAuth;

    public FirebaseService(AppProperties props) {
        this.config = props.firebase();
    }

    @PostConstruct
    void init() {
        if (!StringUtils.hasText(config.projectId())
                || !StringUtils.hasText(config.clientEmail())
                || !StringUtils.hasText(config.privateKey())) {
            log.warn("Firebase is not configured, /auth/firebase/login will be rejected");
            return;
        }
        try {
            FirebaseApp app = FirebaseApp.getApps().isEmpty()
                    ? FirebaseApp.initializeApp(FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(
                                    new ByteArrayInputStream(serviceAccountJson().getBytes(StandardCharsets.UTF_8))))
                            .setProjectId(config.projectId())
                            .build())
                    : FirebaseApp.getInstance();
            this.firebaseAuth = FirebaseAuth.getInstance(app);
        } catch (Exception e) {
            log.error("Failed to initialize Firebase Admin SDK", e);
        }
    }

    public Identity verifyIdToken(String idToken) {
        if (firebaseAuth == null) {
            throw ApiException.unauthorized("Firebase is not configured");
        }
        try {
            FirebaseToken t = firebaseAuth.verifyIdToken(idToken);
            return new Identity(t.getUid(), t.getEmail(), t.getName(), t.getPicture());
        } catch (FirebaseAuthException e) {
            if (e.getAuthErrorCode() == AuthErrorCode.EXPIRED_ID_TOKEN) {
                throw ApiException.unauthorized("Firebase token expired");
            }
            throw ApiException.unauthorized("Invalid token!");
        } catch (IllegalArgumentException e) {
            throw ApiException.unauthorized("Invalid token!");
        }
    }

    /** Env thường chứa private key với "\n" dạng chữ; đổi lại thành xuống dòng thật. */
    private String serviceAccountJson() {
        String key = config.privateKey().replace("\\n", "\n");
        return "{\"type\":\"service_account\",\"project_id\":" + quote(config.projectId())
                + ",\"client_id\":\"unused\",\"private_key_id\":\"unused\"" // thư viện bắt buộc có; verifyIdToken không dùng
                + ",\"client_email\":" + quote(config.clientEmail())
                + ",\"private_key\":" + quote(key)
                + ",\"token_uri\":\"https://oauth2.googleapis.com/token\"}";
    }

    private static String quote(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }
}
