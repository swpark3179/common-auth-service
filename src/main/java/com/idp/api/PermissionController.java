package com.idp.api;

import com.idp.dto.request.PermissionRequest;
import com.idp.dto.response.PermissionResponse;
import com.idp.security.UserDetailsImpl;
import com.idp.service.PermissionService;
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

@Tag(name = "권한 관리", description = "리소스 권한(URI 패턴+메서드) 관리 (ADMIN 전용)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    public ResponseEntity<Page<PermissionResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(permissionService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PermissionResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(permissionService.findById(id));
    }

    @PostMapping
    public ResponseEntity<PermissionResponse> create(
            @Valid @RequestBody PermissionRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(permissionService.create(request, currentUser.getUserId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        permissionService.delete(id, currentUser.getUserId());
        return ResponseEntity.noContent().build();
    }
}
