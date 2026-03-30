package com.idp.api;

import com.idp.dto.request.UserCreateRequest;
import com.idp.dto.response.UserResponse;
import com.idp.security.UserDetailsImpl;
import com.idp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "사용자 관리", description = "사용자 CRUD (ADMIN 전용)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class UserController {

    private final UserService userService;

    @Operation(summary = "사용자 목록 조회")
    @GetMapping
    public ResponseEntity<Page<UserResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(userService.findAll(pageable));
    }

    @Operation(summary = "사용자 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @Operation(summary = "사용자 생성")
    @PostMapping
    public ResponseEntity<UserResponse> create(
            @Valid @RequestBody UserCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.create(request, currentUser.getUserId()));
    }

    @Operation(summary = "사용자 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        userService.delete(id, currentUser.getUserId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "계정 활성화/비활성화")
    @PatchMapping("/{id}/enabled")
    public ResponseEntity<UserResponse> setEnabled(
            @PathVariable Long id,
            @RequestParam boolean value,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(userService.setEnabled(id, value, currentUser.getUserId()));
    }

    @Operation(summary = "계정 잠금/해제")
    @PatchMapping("/{id}/locked")
    public ResponseEntity<UserResponse> setLocked(
            @PathVariable Long id,
            @RequestParam boolean value,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(userService.setLocked(id, value, currentUser.getUserId()));
    }
}
