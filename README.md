# Spring WebFlux + R2DBC + H2 스트리밍 예제 프로젝트

이 프로젝트는 Spring Boot, WebFlux, R2DBC, H2 데이터베이스를 사용한 리액티브 웹 애플리케이션의 최소 실행 예제입니다. 특히 Flux를 사용한 비동기 데이터 스트리밍에 초점을 맞추었습니다.

## 기술 스택

- Spring Boot 3.5.5
- Spring WebFlux
- Spring Data R2DBC
- H2 데이터베이스
- R2DBC H2 Driver
- Lombok

## 프로젝트 구조

```
webflux-sample/
├─ src/
│   ├─ main/
│   │   ├─ java/
│   │   │   └─ com/
│   │   │       └─ webflux/
│   │   │           └─ sample/
│   │   │               ├─ config/
│   │   │               │   └─ DataLoader.java
│   │   │               ├─ controller/
│   │   │               │   ├─ UserController.java
│   │   │               │   └─ StreamingController.java
│   │   │               ├─ entity/
│   │   │               │   └─ User.java
│   │   │               ├─ repository/
│   │   │               │   └─ UserRepository.java
│   │   │               ├─ service/
│   │   │               │   ├─ UserService.java
│   │   │               │   └─ SlowQueryService.java
│   │   │               └─ WebfluxSampleApplication.java
│   │   └─ resources/
│   │       ├─ application.properties
│   │       ├─ schema.sql
│   │       └─ static/
│   │           └─ stream-test.html
│   └─ test/
│       └─ java/
└─ build.gradle
```

## 구현된 기능

### 1. 리액티브 데이터 액세스
- R2DBC를 사용한 비동기 데이터베이스 연결
- 리액티브 리포지토리 패턴 구현

### 2. 비동기 데이터 스트리밍
- Flux를 사용한 데이터 스트리밍
- Server-Sent Events(SSE)를 활용한 실시간 데이터 전송
- 데이터가 준비되는 대로 클라이언트에게 전송

### 3. 데이터 로딩 및 부하 테스트
- 대량의 더미 데이터 생성
- 의도적인 지연을 통한 스트리밍 효과 시각화

### 4. 웹 인터페이스
- 스트리밍 테스트를 위한 웹 페이지 제공
- H2 콘솔 접근 기능

## 실행 방법

### 1. 애플리케이션 실행

Windows에서 다음 명령어로 애플리케이션을 실행합니다:

```bash
gradlew.bat bootRun
```

### 2. 웹 접속

애플리케이션이 실행되면 다음 URL로 접속할 수 있습니다:

- 메인 페이지: http://localhost:8080/
- 스트리밍 테스트 페이지: http://localhost:8080/stream-test.html
- H2 콘솔: http://localhost:8080/h2-console

### 3. API 엔드포인트

#### 기본 REST API
- 모든 사용자 조회: GET http://localhost:8080/api/users
- 특정 사용자 조회: GET http://localhost:8080/api/users/{id}
- 이메일로 사용자 조회: GET http://localhost:8080/api/users/email/{email}
- 사용자 생성: POST http://localhost:8080/api/users
- 사용자 수정: PUT http://localhost:8080/api/users/{id}
- 사용자 삭제: DELETE http://localhost:8080/api/users/{id}

#### 스트리밍 API (Server-Sent Events)
- 사용자 데이터 스트리밍: GET http://localhost:8080/api/stream/users
- 시간 정보 무한 스트리밍: GET http://localhost:8080/api/stream/time
- 사용자 데이터 청크 스트리밍: GET http://localhost:8080/api/stream/users/chunked

## H2 콘솔 접속 정보

H2 콘솔에 접속할 때 다음 정보를 사용하세요:

- JDBC URL: `jdbc:h2:mem:webflux_sample`
- 사용자명: `sa`
- 비밀번호: (비어있음)

## 스트리밍 테스트 방법

1. 애플리케이션을 실행합니다.
2. 웹 브라우저에서 http://localhost:8080/stream-test.html 에 접속합니다.
3. 페이지에서 제공하는 각 버튼을 클릭하여 다양한 스트리밍 기능을 테스트할 수 있습니다:
   - 사용자 데이터 스트리밍: 사용자 데이터가 하나씩 스트리밍됩니다.
   - 시간 정보 무한 스트리밍: 1초마다 현재 시간이 스트리밍됩니다.
   - 사용자 데이터 청크 스트리밍: 10명씩 그룹화하여 스트리밍됩니다.

