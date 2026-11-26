# User Schema Design

## `users` 테이블 명세

| 컬럼 | 자료형 | NULL 제약조건 | 명시된 기본값 | 키 및 제약조건 | 설명 |
|---|---|---|---|---|---|
| `id` | `BINARY(16)` | `NOT NULL` | — | `PRIMARY KEY` | 회원 식별자(UUID) |
| `email_hash` | `VARCHAR(255)` | `NOT NULL` | — | `UNIQUE` | 이메일 해시값 |
| `password_hash` | `VARCHAR(255)` | `NOT NULL` | — | — | 비밀번호 해시값 |
| `nickname` | `VARCHAR(30)` | `NOT NULL` | — | `UNIQUE` | 닉네임 |
| `role` | `VARCHAR(20)` | `NOT NULL` | `'USER'` | `chk_users_role` | 회원 권한 |
| `status` | `VARCHAR(20)` | `NOT NULL` | `'ACTIVE'` | `chk_users_status` | 계정 상태 |
| `profile_image_url` | `VARCHAR(500)` | `NULL` | — | — | 프로필 이미지 URL |
| `suspended_until` | `DATETIME` | `NULL` | — | — | 이용 정지 만료 일시 |
| `last_login_at` | `DATETIME` | `NULL` | — | — | 마지막 로그인 일시 |
| `created_at` | `DATETIME` | `NOT NULL` | — | — | 계정 생성 일시 |
| `updated_at` | `DATETIME` | `NOT NULL` | — | — | 계정 정보 수정 일시 |
| `deleted_at` | `DATETIME` | `NULL` | — | — | 회원 탈퇴 일시 |

| CHECK 제약조건 | 조건 |
|---|---|
| `chk_users_role` | `role IN ('USER', 'ADMIN')` |
| `chk_users_status` | `status IN ('ACTIVE', 'SUSPENDED', 'BANNED', 'WITHDRAWN')` |

## 1. Overview

회원 정보, 인증 정보, 접근 권한 및 계정 상태를 관리하는 테이블입니다.

## 2. Design Decisions

### 2.1 PK — UUID (BINARY(16))

**설계 배경**

순차적으로 증가하는 BIGINT 대신 UUID를 회원 식별자로 사용합니다.

**선택 이유**

- 데이터베이스에 접근하지 않고 애플리케이션에서 식별자 생성 가능
- 외부에 노출되는 식별자의 순차적인 값 추측 방지
- 향후 분산 환경에서 독립적인 식별자 생성 가능

**저장 방식**

UUID를 CHAR(36)이 아닌 BINARY(16)으로 저장하여 저장 공간과 인덱스 크기를 줄입니다.

**Trade-off**

- BIGINT(8Byte)보다 큰 저장 공간 사용
- 무작위 UUID 사용 시 인덱스 페이지 분할 및 INSERT 성능 저하 가능성

### 2.2 Email — Hash & UNIQUE

**선택 이유**

- 이메일 원문 저장 최소화
- UNIQUE 제약조건을 통한 중복 가입 방지
- 동일한 이메일을 동일한 해시값으로 변환하여 회원 식별

### 2.3 Password — Hash

**선택 이유**

- 비밀번호 원문 저장 방지
- 단방향 비밀번호 해싱을 통한 계정 정보 보호

### 2.4 Role & Status — VARCHAR + CHECK

**비교 대상**

- MySQL ENUM
- VARCHAR + CHECK

**선택 이유**

- Java Enum과 문자열 기반 매핑
- 데이터베이스 종속성 완화
- CHECK 제약조건을 통한 허용되지 않은 값의 저장 방지

### 2.5 Account Suspension — suspended_until

**선택 이유**

- 기간제 이용 정지와 영구 이용 정지 구분
- 이용 정지 만료 시점 확인

### 2.6 Timestamp — JPA Auditing

**비교 대상**

- DB DEFAULT CURRENT_TIMESTAMP 및 ON UPDATE
- JPA Auditing

**선택 이유**

- 생성일 및 수정일 관리 책임을 애플리케이션으로 일원화
- BaseEntity를 통한 공통 필드 관리
- DB의 NOT NULL 제약조건을 통한 필수 데이터 누락 방지

**Trade-off**

- 직접 SQL 및 JPQL 벌크 UPDATE 실행 시 수정일을 명시적으로 관리해야 함

### 2.7 Soft Delete — deleted_at

**선택 이유**

- 회원 탈퇴 시 물리적 삭제 대신 논리적 삭제 처리
- 연관된 후기 및 게시글의 참조 무결성 유지
- 회원 탈퇴 시점 관리

**고려 사항**

- 탈퇴 회원의 개인정보 삭제 정책
- 이메일 및 닉네임 재사용 정책
- 탈퇴 회원의 인증 및 접근 제한
