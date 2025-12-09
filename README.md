# Community Backend API

커뮤니티 게시판 백엔드 서버 (Spring Boot)

## 📋 목차

- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [환경 설정](#환경-설정)
- [실행 방법](#실행-방법)
- [API 엔드포인트](#api-엔드포인트)
- [데이터베이스 설정](#데이터베이스-설정)
- [보안 설정](#보안-설정)

## 🛠 기술 스택

- **Java**: 21
- **Spring Boot**: 3.5.6
- **Spring Security**: JWT 기반 인증
- **Spring Data JPA**: 데이터베이스 ORM
- **MySQL**: 데이터베이스
- **QueryDSL**: 동적 쿼리
- **Lombok**: 보일러플레이트 코드 제거
- **Gradle**: 빌드 도구

## 📁 프로젝트 구조

```
community/
├── src/main/java/com/springboot/project/community/
│   ├── config/              # 설정 클래스
│   │   ├── SecurityConfig.java      # Spring Security 설정
│   │   ├── WebConfig.java           # CORS, 인터셉터 설정
│   │   ├── JpaConfig.java           # JPA 설정
│   │   └── QuerydslConfig.java      # QueryDSL 설정
│   ├── controller/          # REST API 컨트롤러
│   │   ├── auth/            # 인증 관련
│   │   ├── board/           # 게시글 관련
│   │   ├── comment/         # 댓글 관련
│   │   └── like/            # 좋아요 관련
│   ├── service/             # 비즈니스 로직
│   ├── repository/          # 데이터 접근 계층
│   ├── entity/              # JPA 엔티티
│   ├── dto/                 # 데이터 전송 객체
│   ├── security/            # 보안 관련
│   │   └── jwt/             # JWT 토큰 처리
│   ├── exception/           # 예외 처리
│   └── util/                # 유틸리티
└── src/main/resources/
    └── application.yml       # 애플리케이션 설정
```

## ⚙️ 환경 설정

### 필수 요구사항

- Java 21 이상
- MySQL 8.0 이상
- Gradle 8.14 이상

### application.yml 설정

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/COMMUNITY_DB?serverTimezone=UTC&useSSL=false
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: none
    properties:
      hibernate:
        format_sql: true

jwt:
  secret: your_jwt_secret_key
  access-token-expiration: 900000      # 15분
  refresh-token-expiration: 604800000  # 7일
```

## 🚀 실행 방법

### 1. 데이터베이스 설정

```sql
CREATE DATABASE COMMUNITY_DB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 의존성 설치 및 빌드

```bash
cd community
./gradlew clean build
```

### 3. 서버 실행

```bash
./gradlew bootRun
```

또는

```bash
java -jar build/libs/community-0.0.1-SNAPSHOT.jar
```

서버는 기본적으로 `http://localhost:8080`에서 실행됩니다.

## 📡 API 엔드포인트

### 인증 API (`/api/auth`)

#### 회원가입
```http
POST /api/auth
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password1234",
  "nickname": "nickname",
  "image": "base64_image_string" (optional)
}
```

**응답:**
```json
{
  "success": true,
  "message": "회원가입 성공",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "nickname": "nickname"
  }
}
```

#### 로그인
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password1234"
}
```

**응답:**
```json
{
  "success": true,
  "message": "로그인 성공",
  "accessToken": "jwt_token",
  "tokenType": "Bearer",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "nickname": "nickname"
  }
}
```

#### Access Token 재발급
```http
POST /api/auth/refresh
```

#### 로그아웃
```http
POST /api/auth/logout
Authorization: Bearer {accessToken}
```

#### 현재 사용자 정보 조회
```http
GET /api/auth/me
Authorization: Bearer {accessToken}
```

#### 토큰 검증
```http
GET /api/auth/check
Authorization: Bearer {accessToken}
```

#### 이메일 중복 확인
```http
GET /api/auth/check-email?email=user@example.com
```

#### 닉네임 중복 확인
```http
GET /api/auth/check-nickname?nickname=nickname
```

### 게시글 API (`/api/v1/boards`)

#### 게시글 작성
```http
POST /api/v1/boards?userId={userId}
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "title": "게시글 제목",
  "contents": "게시글 내용",
  "imageUrls": ["url1", "url2"] (optional)
}
```

#### 게시글 목록 조회
```http
GET /api/v1/boards?page=0&size=10
```

#### 게시글 상세 조회
```http
GET /api/v1/boards/{postId}
```

#### 게시글 수정
```http
PUT /api/v1/boards/{postId}?userId={userId}
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "title": "수정된 제목",
  "contents": "수정된 내용"
}
```

### 댓글 API (`/api/v1/boards/comments`)

#### 댓글 작성
```http
POST /api/v1/boards/comments/{postId}
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "contents": "댓글 내용"
}
```

#### 댓글 목록 조회
```http
GET /api/v1/boards/comments/{postId}
```

#### 댓글 수정
```http
PUT /api/v1/boards/comments/{postId}/{commentId}?userId={userId}
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "contents": "수정된 댓글 내용"
}
```

### 좋아요 API (`/api/v1/likes`)

#### 좋아요 토글
```http
POST /api/v1/likes/{postId}
Authorization: Bearer {accessToken}
```

**응답:**
```json
{
  "postId": 1,
  "isLiked": true,
  "likeCount": 5
}
```

## 🗄 데이터베이스 설정

### 주요 테이블

- **USERS**: 사용자 정보
- **BOARD**: 게시글
- **BOARD_STATS**: 게시글 통계 (조회수, 좋아요 수, 댓글 수)
- **BOARD_IMAGE**: 게시글 이미지
- **COMMENT**: 댓글
- **BOARD_LIKE**: 게시글 좋아요
- **REFRESH_TOKEN**: 리프레시 토큰
- **IMAGE_FILE**: 프로필 이미지


### 데이터베이스 연결

`application.yml`에서 데이터베이스 연결 정보를 설정하세요:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/COMMUNITY_DB?serverTimezone=UTC&useSSL=false
    username: your_username
    password: your_password
```

## 🔐 보안 설정

### JWT 인증

- **Access Token**: 15분 유효
- **Refresh Token**: 7일 유효 (쿠키에 저장)
- **토큰 형식**: `Bearer {token}`

### 인증이 필요한 API

대부분의 API는 JWT 토큰이 필요합니다. 요청 헤더에 다음을 추가

```
Authorization: Bearer {accessToken}
```

### 인증이 필요 없는 API

- `POST /api/auth` (회원가입)
- `POST /api/auth/login` (로그인)
- `GET /api/auth/check-email` (이메일 중복 확인)
- `GET /api/auth/check-nickname` (닉네임 중복 확인)
- `GET /api/v1/boards` (게시글 목록 조회)
- `GET /api/v1/boards/{postId}` (게시글 상세 조회)
- `GET /api/v1/boards/comments/{postId}` (댓글 목록 조회)

### CORS 설정

현재 허용된 Origin:
- `http://localhost:3000`
- `http://127.0.0.1:3000`

프로덕션 환경에서는 `SecurityConfig.java`에서 Origin을 수정하세요.

## 🐛 예외 처리

모든 예외는 `GlobalExceptionHandler`에서 처리됩니다.

### 예외 응답 형식

```json
{
  "success": false,
  "message": "에러 메시지",
  "error": "ExceptionType" (optional)
}
```

### 주요 예외

- **IllegalArgumentException**: 잘못된 인자 (400 Bad Request)
- **RuntimeException**: 런타임 예외 (500 Internal Server Error)
- **MethodArgumentNotValidException**: 유효성 검증 실패 (400 Bad Request)

## 📝 개발 가이드

### 빌드

```bash
./gradlew clean build
```

### 테스트 실행

```bash
./gradlew test
```

### QueryDSL 생성

```bash
./gradlew compileJava
```

생성된 파일은 `build/generated/sources/annotationProcessor/`에 위치합니다.

## 🔧 주요 설정 파일

- `application.yml`: 애플리케이션 설정
- `SecurityConfig.java`: Spring Security 설정
- `WebConfig.java`: CORS 및 인터셉터 설정
- `JpaConfig.java`: JPA Auditing 설정

## 📌 주의사항

1. **JWT Secret Key**: 프로덕션 환경에서는 반드시 강력한 시크릿 키를 사용하세요.
2. **데이터베이스 비밀번호**: `application.yml`에 실제 비밀번호를 하드코딩하지 마세요. 환경 변수 사용을 권장합니다.
3. **CORS 설정**: 프로덕션 환경에서는 허용된 Origin을 명확히 지정하세요.
4. **세션 스토어**: 현재는 `none`으로 설정되어 있습니다. 프로덕션에서는 Redis 사용을 권장합니다.

## 📞 문의

문제가 발생하면 이슈를 등록해주세요.
