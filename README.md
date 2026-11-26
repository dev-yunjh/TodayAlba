![귀여운 오늘도알바 로고.png](docs/github/%EA%B7%80%EC%97%AC%EC%9A%B4%20%EC%98%A4%EB%8A%98%EB%8F%84%EC%95%8C%EB%B0%94%20%EB%A1%9C%EA%B3%A0.png)

---

## 목차

| 번호 | 항목 |
|---|---|
| 1 | [Description](#1-description) |
| 2 | [Project Overview](#2-project-overview) |
| 3 | [Features](#3-features) |
| 4 | [Tech Stack](#4-tech-stack) |
| 5 | [Technical Decisions](#5-technical-decisions) |
| 5.1 | [도메인 중심 패키지 구조](#51-도메인-중심-패키지-구조) |
| 5.2 | [SQL 기반 데이터베이스 스키마 설계](#52-sql-기반-데이터베이스-스키마-설계) |
| 5.3 | [서버 DB와 로컬 DB의 역할 분리](#53-서버-db와-로컬-db의-역할-분리) |
| 6 | [System Architecture](#6-system-architecture) |
| 7 | [Database Design](#7-database-design) |
| 7.1 | [ERD](#71-erd) |
| 7.2 | [Table Specification](#72-table-specification) |
| 7.3 | [Schema Migration](#73-schema-migration) |
| 8 | [API Documentation](#8-api-documentation) |
| 9 | [Troubleshooting](#9-troubleshooting) |
| 10 | [Roadmap](#10-roadmap) |

---

### 1. Description


오늘도알바는 아르바이트 근무지별 일정 및 급여 관리, 근무일별 메모 기록, 근무지 후기 공유, 커뮤니티 기능을 제공하는 아르바이트 종합 플랫폼입니다.

실제 서비스 출시 및 운영을 목표로 하는 장기 개인 프로젝트로, 초기에는 핵심 기능을 중심으로 MVP를 개발하고, 사용자 피드백과 서비스 운영 경험을 바탕으로 지속적인 기능 개선 및 확장을 진행할 계획입니다.

---

### 2. Project Overview



| 구분           | 내용                                                       |
|----------------|------------------------------------------------------------|
| 프로젝트명     | 오늘도알바 (TodayAlba)                                     |
| 개발 기간      | 2026.11 ~ 진행 중                                          |
| 개발 인원      | 1인 개발                                                   |
| 담당 역할      | 서비스 기획, UI/UX 디자인, DB 설계, 웹·백엔드 개발 및 운영 |
| 모바일 앱 개발 | OpenAI Codex 활용                                          |
| 지원 플랫폼    | Web, Android, iOS                                          |
| 개발 방식      | MVP 기반 점진적 개발                                       |

---

### 3. Features

#### 3.1 근무 관리

- 근무지 등록 및 관리
- 근무 일정 등록 및 캘린더 조회
- 근무지별 예상 급여 계산
- 근무일별 메모 및 특이사항 기록

#### 3.2 아르바이트 후기

- 근무지 후기 작성 및 조회
- 근무 환경 및 경험 공유

#### 3.3 커뮤니티

- 게시글 및 댓글
- 아르바이트 관련 정보 공유
- 게시글 및 댓글 신고

---

### 4. Tech Stack

---

### 5. Technical Decisions

#### 5.1 도메인 중심 패키지 구조

#### 5.2 SQL 기반 데이터베이스 스키마 설계

JPA의 자동 DDL 생성 기능에 의존하지 않고, 서비스 요구사항을 기반으로 SQL을 직접 작성하여 데이터베이스 스키마를 설계합니다.

- 데이터 특성을 고려한 자료형 및 컬럼 정의
- PK, FK, UNIQUE, CHECK 제약조건을 통한 데이터 무결성 보장
- 조회 패턴을 고려한 인덱스 설계
- Flyway를 활용한 스키마 버전 및 변경 이력 관리

#### 5.3 서버 DB와 로컬 DB의 역할 분리

---

### 6. System Architecture

---

### 7. Database Design

#### 7.1 ERD

#### 7.2 Table Specification

| 테이블 | 설명 |
|---|---|
| users | 회원 정보 및 계정 상태 관리 |

#### 7.3 Schema Migration

| Version | Description |
|---|---|
| V1 | 회원 도메인 초기 스키마 생성 |

[SQL Migration Files](./src/main/resources/db/migration)

---

### 8. API Documentation

---

### 9. Troubleshooting

---

### 10. Roadmap

- [ ] Phase 1. 근무 및 급여 관리 MVP 개발
- [ ] Phase 2. 아르바이트 후기 및 커뮤니티 MVP 개발
- [ ] Phase 3. Android 및 iOS 서비스 출시
- [ ] Phase 4. 사용자 피드백 기반 서비스 개선
- [ ] Phase 5. 서비스 확장 및 수익화

---
