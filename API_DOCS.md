# IDP Auth Service API 전체 목록 및 기능 요약

이 문서는 시스템 내에 존재하는 전체 API 목록과 간략한 기능을 정리한 것입니다.

## 1. 사용자 관리 (User Management)
사용자 CRUD 기능을 제공합니다. 이 기능은 `ADMIN` 권한을 가진 사용자 전용입니다.

| HTTP Method | API Path | Summary (요약) |
| :--- | :--- | :--- |
| `GET` | `/api/users` | 사용자 목록 조회 (페이징 지원) |
| `GET` | `/api/users/{id}` | 사용자 단건 조회 |
| `POST` | `/api/users` | 새로운 사용자 생성 |
| `DELETE` | `/api/users/{id}` | 특정 사용자 삭제 |
| `PATCH` | `/api/users/{id}/enabled` | 계정 활성화/비활성화 상태 설정 |
| `PATCH` | `/api/users/{id}/locked` | 계정 잠금/해제 상태 설정 |

## 2. 역할 관리 (Role Management)
사용자의 역할 및 해당 역할에 부여된 권한을 관리합니다. 이 기능은 `ADMIN` 권한을 가진 사용자 전용입니다.

| HTTP Method | API Path | Summary (요약) |
| :--- | :--- | :--- |
| `GET` | `/api/roles` | 역할 목록 조회 (페이징 지원) |
| `GET` | `/api/roles/{id}` | 역할 단건 조회 |
| `POST` | `/api/roles` | 새로운 역할 생성 |
| `PUT` | `/api/roles/{id}` | 기존 역할 수정 |
| `DELETE` | `/api/roles/{id}` | 특정 역할 삭제 |
| `POST` | `/api/roles/{roleId}/permissions/{permissionId}` | 특정 역할에 권한 추가 |
| `DELETE` | `/api/roles/{roleId}/permissions/{permissionId}` | 특정 역할에서 권한 제거 |

## 3. 권한 관리 (Permission Management)
시스템 리소스에 대한 접근 권한(URI 패턴 및 HTTP 메서드 등)을 관리합니다. 이 기능은 `ADMIN` 권한을 가진 사용자 전용입니다.

| HTTP Method | API Path | Summary (요약) |
| :--- | :--- | :--- |
| `GET` | `/api/permissions` | 권한 목록 조회 (페이징 지원) |
| `GET` | `/api/permissions/{id}` | 권한 단건 조회 |
| `POST` | `/api/permissions` | 새로운 권한 생성 |
| `DELETE` | `/api/permissions/{id}` | 특정 권한 삭제 |

## 4. 인증 (Authentication)
로그인, 로그아웃, 토큰 발급 및 갱신 기능을 제공합니다.

| HTTP Method | API Path | Summary (요약) |
| :--- | :--- | :--- |
| `POST` | `/api/auth/login` | 아이디와 비밀번호로 로그인하여 Access Token 및 Refresh Token 발급 |
| `POST` | `/api/auth/refresh` | Refresh Token을 사용하여 새로운 Access Token과 Refresh Token 쌍을 발급 (Rotation 지원) |
| `POST` | `/api/auth/logout` | 로그아웃 처리, 현재 Access Token 블랙리스트 등록 및 Refresh Token 폐기 (인증 필요) |
| `GET` | `/api/auth/me` | 현재 인증된 사용자의 정보 조회 (인증 필요) |

## 5. APISIX 인증 (APISIX Integration)
Apache APISIX API Gateway와의 연동을 위해 사용되는 외부 인증용 엔드포인트입니다. 외부에서는 직접 접근할 수 없으며 Gateway에서만 호출해야 합니다.

| HTTP Method | API Path | Summary (요약) |
| :--- | :--- | :--- |
| `POST` | `/apisix/auth` | APISIX `ext-plugin-pre-req` 플러그인을 통해 들어오는 각 요청의 권한을 검증 |
