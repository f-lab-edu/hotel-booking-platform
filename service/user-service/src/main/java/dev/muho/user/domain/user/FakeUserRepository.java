package dev.muho.user.domain.user;

import dev.muho.user.domain.user.dto.api.UserRoleUpdateRequest;
import dev.muho.user.domain.user.dto.api.UserSignupRequest;
import dev.muho.user.domain.user.dto.api.UserUpdateRequest;
import dev.muho.user.domain.user.dto.api.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class FakeUserRepository {

    private final Map<Long, UserResponse> db = new HashMap<>();
    private long currentId = 1L;

    public Page<UserResponse> findAll(Pageable pageable) {
        List<UserResponse> users = new ArrayList<>(db.values());
        users.sort(Comparator.comparing(UserResponse::getId));

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), users.size());

        if (start > users.size()) {
            return Page.empty(pageable);
        }

        List<UserResponse> content = users.subList(start, end);
        return new PageImpl<>(content, pageable, users.size());
    }

    public UserResponse findById(Long id) {
        return db.get(id);
    }

    public UserResponse save(UserSignupRequest request) {
        UserResponse user = new UserResponse(currentId++, request.getEmail(), request.getName(), request.getPhone(), UserRole.CUSTOMER);
        db.put(user.getId(), user);
        return user;
    }

    public UserResponse update(Long id, UserUpdateRequest request) {
        UserResponse existingUser = db.get(id);
        UserResponse updatedUser = new UserResponse(id, existingUser.getEmail(), request.getName(), request.getPhone(), existingUser.getRole());
        db.put(id, updatedUser);
        return updatedUser;
    }

    public UserResponse updateRole(Long id, UserRoleUpdateRequest request) {
        UserResponse existingUser = db.get(id);
        UserResponse updatedUser = new UserResponse(id, existingUser.getEmail(), existingUser.getName(), existingUser.getPhone(), request.getRole());
        db.put(id, updatedUser);
        return updatedUser;
    }

    public void deleteById(Long id) {
        db.remove(id);
    }

    public void clear() {
        db.clear();
        currentId = 1L;
    }
}
