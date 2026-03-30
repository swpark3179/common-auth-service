package com.idp.api;

import com.idp.dto.request.RoleRequest;
import com.idp.dto.response.RoleResponse;
import com.idp.security.UserDetailsImpl;
import com.idp.service.RoleService;
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

@Tag(name = "역할 관리", description = "역할 및 권한 매핑 관리 (ADMIN 전용)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public ResponseEntity<Page<RoleResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(roleService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.findById(id));
    }

    @PostMapping
    public ResponseEntity<RoleResponse> create(
            @Valid @RequestBody RoleRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roleService.create(request, currentUser.getUserId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoleResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(roleService.update(id, request, currentUser.getUserId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        roleService.delete(id, currentUser.getUserId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "역할에 권한 추가")
    @PostMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<RoleResponse> assignPermission(
            @PathVariable Long roleId,
            @PathVariable Long permissionId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(roleService.assignPermission(roleId, permissionId, currentUser.getUserId()));
    }

    @Operation(summary = "역할에서 권한 제거")
    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<RoleResponse> revokePermission(
            @PathVariable Long roleId,
            @PathVariable Long permissionId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(roleService.revokePermission(roleId, permissionId, currentUser.getUserId()));
    }
}
