package com.kruzetech.auth.service;

import com.kruzetech.auth.controller.dto.UserDtos.CreateUserRequest;
import com.kruzetech.auth.controller.dto.UserDtos.LeaderboardEntryResponse;
import com.kruzetech.auth.controller.dto.UserDtos.LeaderboardResponse;
import com.kruzetech.auth.controller.dto.UserDtos.ProgressionResponse;
import com.kruzetech.auth.controller.dto.UserDtos.UpdateProgressionRequest;
import com.kruzetech.auth.controller.dto.UserDtos.UpdateUserRequest;
import com.kruzetech.auth.controller.dto.UserDtos.UserResponse;
import com.kruzetech.auth.core.PageResponse;
import com.kruzetech.auth.core.exception.ApiException;
import com.kruzetech.auth.entity.User;
import com.kruzetech.auth.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse create(CreateUserRequest req) {
        if (users.existsByEmail(req.email())) {
            throw ApiException.conflict("User with this email already exists");
        }
        User user = new User();
        user.setEmail(req.email());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setFirstName(req.firstName());
        user.setLastName(req.lastName());
        return UserResponse.from(users.save(user));
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> findAll(int page, int limit, String search) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 1) - 1, limit > 0 ? limit : 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        var result = StringUtils.hasText(search) ? users.search(search.trim(), pageable) : users.findAll(pageable);
        return PageResponse.of(result, UserResponse::from);
    }

    @Transactional(readOnly = true)
    public UserResponse findOne(String id) {
        return UserResponse.from(getOrThrow(id));
    }

    @Transactional
    public UserResponse update(String id, UpdateUserRequest req) {
        User user = getOrThrow(id);
        if (req.email() != null && !req.email().equals(user.getEmail())) {
            if (users.existsByEmail(req.email())) {
                throw ApiException.conflict("User with this email already exists");
            }
            user.setEmail(req.email());
        }
        if (req.firstName() != null) {
            user.setFirstName(req.firstName());
        }
        if (req.lastName() != null) {
            user.setLastName(req.lastName());
        }
        if (StringUtils.hasText(req.password())) {
            user.setPassword(passwordEncoder.encode(req.password()));
        }
        if (req.avatar() != null) {
            user.setAvatar(req.avatar());
        }
        return UserResponse.from(users.saveAndFlush(user));
    }

    /** App Flutter gửi {name, avatar}: tách name thành first/last để khớp schema hiện tại. */
    @Transactional
    public UserResponse updateProfile(String id, String name, String avatar) {
        User user = getOrThrow(id);
        if (StringUtils.hasText(name)) {
            String[] parts = name.trim().split("\\s+", 2);
            user.setFirstName(parts[0]);
            user.setLastName(parts.length > 1 ? parts[1] : null);
        }
        if (avatar != null) {
            user.setAvatar(avatar.isBlank() ? null : avatar);
        }
        return UserResponse.from(users.saveAndFlush(user));
    }

    @Transactional(readOnly = true)
    public ProgressionResponse getProgression(String id) {
        return ProgressionResponse.from(getOrThrow(id));
    }

    /** Cộng EXP / streak / số liệu học tập. Chuẩn level: level * 100 EXP để lên cấp. */
    @Transactional
    public ProgressionResponse addProgression(String id, UpdateProgressionRequest req) {
        User user = getOrThrow(id);
        Instant now = Instant.now();

        int gained = req != null && req.expGained() != null ? Math.max(req.expGained(), 0) : 0;
        boolean cardStudied = req != null && Boolean.TRUE.equals(req.cardStudied());
        boolean wordMastered = req != null && Boolean.TRUE.equals(req.wordMastered());
        boolean correctExercise = req != null && Boolean.TRUE.equals(req.correctExercise());
        if (gained == 0 && (cardStudied || correctExercise)) {
            gained = wordMastered ? 15 : 10;
        }

        int newTotal = Math.max(user.getTotalExp(), 0) + gained;
        int newCurrent = Math.max(user.getCurrentExp(), 0) + gained;
        int level = Math.max(user.getLevel(), 1);
        while (newCurrent >= level * 100) {
            newCurrent -= level * 100;
            level += 1;
        }
        user.setTotalExp(newTotal);
        user.setCurrentExp(newCurrent);
        user.setLevel(level);

        if (cardStudied || correctExercise) {
            user.setTotalReviews(Math.max(user.getTotalReviews(), 0) + 1);
        }
        if (wordMastered) {
            user.setWordsMastered(Math.max(user.getWordsMastered(), 0) + 1);
        }
        applyStreak(user, now, cardStudied || correctExercise || gained > 0);
        return ProgressionResponse.from(users.saveAndFlush(user));
    }

    private void applyStreak(User user, Instant now, boolean studied) {
        if (!studied) return;
        LocalDate today = now.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate last = user.getLastStudyDate() != null
                ? user.getLastStudyDate().atZone(ZoneOffset.UTC).toLocalDate()
                : null;
        if (last == null) {
            user.setStreak(1);
        } else if (last.equals(today)) {
            if (user.getStreak() <= 0) user.setStreak(1);
        } else if (last.plusDays(1).equals(today)) {
            user.setStreak(Math.max(user.getStreak(), 0) + 1);
        } else {
            user.setStreak(1);
        }
        user.setLastStudyDate(now);
    }

    @Transactional(readOnly = true)
    public LeaderboardResponse leaderboard(String currentUserId, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        List<User> top = users.findLeaderboard(PageRequest.of(0, safeLimit));
        long total = users.countActiveUsers();

        List<LeaderboardEntryResponse> entries = new ArrayList<>();
        for (int i = 0; i < top.size(); i++) {
            User u = top.get(i);
            entries.add(toEntry(i + 1, u, u.getId().equals(currentUserId)));
        }

        User me = getOrThrow(currentUserId);
        long ahead = users.countUsersAhead(
                Math.max(me.getTotalExp(), 0),
                Math.max(me.getWordsMastered(), 0),
                me.getCreatedAt() != null ? me.getCreatedAt() : Instant.now());
        LeaderboardEntryResponse mine = toEntry((int) ahead + 1, me, true);

        List<LeaderboardEntryResponse> topThree =
                entries.size() > 3 ? List.copyOf(entries.subList(0, 3)) : List.copyOf(entries);
        List<LeaderboardEntryResponse> rest = new ArrayList<>();
        for (LeaderboardEntryResponse e : entries) {
            if (e.rank() > 3) rest.add(e);
        }
        boolean mineInList = entries.stream().anyMatch(e -> e.id().equals(currentUserId));
        if (!mineInList) rest.add(mine);
        return new LeaderboardResponse(topThree, mine, rest, (int) total);
    }

    private LeaderboardEntryResponse toEntry(int rank, User u, boolean isMe) {
        return new LeaderboardEntryResponse(
                rank,
                u.getId(),
                u.displayName(),
                u.getAvatar(),
                Math.max(u.getTotalExp(), 0),
                Math.max(u.getWordsMastered(), 0),
                Math.max(u.getStreak(), 0),
                u.rankTitle(),
                isMe);
    }

    @Transactional
    public void remove(String id) {
        users.delete(getOrThrow(id));
    }

    private User getOrThrow(String id) {
        return users.findById(id).orElseThrow(() -> ApiException.notFound("User not found"));
    }
}
