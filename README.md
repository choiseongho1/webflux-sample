# Spring WebFlux + R2DBC + MySQL 샘플 프로젝트

이 프로젝트는 Spring Boot, WebFlux, R2DBC, MySQL을 사용한 리액티브 웹 애플리케이션의 최소 실행 예제입니다. Spring WebFlux의 핵심 개념과 사용법을 실습하기 위한 예제 코드를 포함하고 있습니다.

## 기술 스택

- Spring Boot 3.5.5
- Spring WebFlux
- Spring Data R2DBC
- MySQL
- R2DBC MySQL Driver
- Lombok

## 프로젝트 구조

```
webflux-sample/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── webflux/
│   │   │           └── sample/
│   │   │               ├── controller/
│   │   │               │   └── UserController.java
│   │   │               ├── entity/
│   │   │               │   └── User.java
│   │   │               ├── repository/
│   │   │               │   └── UserRepository.java
│   │   │               ├── service/
│   │   │               │   └── UserService.java
│   │   │               └── WebfluxSampleApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── schema.sql
│   │       └── data.sql
│   └── test/
│       └── java/
└── build.gradle
```

## 실행 방법

### 1. MySQL 데이터베이스 준비

MySQL 서버를 실행하고 다음 명령어로 데이터베이스를 생성합니다:

```sql
CREATE DATABASE webflux_sample;
```

### 2. 애플리케이션 설정

`src/main/resources/application.properties` 파일에서 데이터베이스 연결 정보를 수정합니다:

```properties
spring.r2dbc.url=r2dbc:pool:mysql://localhost:3306/webflux_sample
spring.r2dbc.username=root
spring.r2dbc.password=password
```

### 3. 애플리케이션 실행

다음 명령어로 애플리케이션을 실행합니다:

```bash
./gradlew bootRun
```

Windows에서는:

```bash
gradlew.bat bootRun
```

### 4. API 테스트

애플리케이션이 실행되면 다음 엔드포인트를 사용할 수 있습니다:

- 모든 사용자 조회: GET http://localhost:8080/api/users
- 특정 사용자 조회: GET http://localhost:8080/api/users/{id}
- 이메일로 사용자 조회: GET http://localhost:8080/api/users/email/{email}
- 사용자 생성: POST http://localhost:8080/api/users
- 사용자 수정: PUT http://localhost:8080/api/users/{id}
- 사용자 삭제: DELETE http://localhost:8080/api/users/{id}

#### 사용자 생성 요청 예시 (POST)

```json
{
  "name": "새사용자",
  "email": "new@example.com"
}
```

## 참고사항

- 애플리케이션 시작 시 `schema.sql`과 `data.sql` 파일을 통해 테이블 생성 및 초기 데이터가 자동으로 삽입됩니다.
- R2DBC는 JPA와 달리 리액티브 프로그래밍 모델을 지원하며, `Mono`와 `Flux` 타입을 사용하여 비동기적으로 데이터를 처리합니다.

## Spring WebFlux 실습

### 1️⃣ 개념
- Spring 5 부터 도입된 비동기 논블로킹(reactive) 웹 프레임워크
- Reactive Streams 표준을 구현한 Reactor 라이브러리(Mono, Flux) 기반
- 기존 Spring MVC의 Thread-per-request 모델과 달리, 이벤트 루프 기반으로 요청을 처리
- 소수의 스레드로도 수천 개의 동시 연결을 효율적으로 처리 가능

### 2️⃣ 동작 원리
- **서버 런타임**
  - 기본: Netty (이벤트 루프 기반)
  - 그 외 Jetty, Undertow, Tomcat(서블릿 3.1 이상) 지원
- **Reactive Streams**
  - Publisher가 데이터를 제공, Subscriber가 구독하여 소비
  - 데이터가 준비될 때마다 비동기적으로 전달
- **리턴 타입**
  - Mono<T>: 0~1개의 데이터
  - Flux<T>: 0~N개의 데이터 스트림
  - → API 응답을 비동기적으로 처리
- **프로그래밍 모델**
  - 어노테이션 기반: @RestController, @GetMapping
  - 함수형 라우팅: RouterFunction, HandlerFunction

### 3️⃣ 장점
- 적은 리소스로 고동시성 처리 (이벤트 루프 모델)
- 실시간 스트리밍 (SSE, WebSocket 지원)
- 외부 API 연동 최적화 (WebClient, 팬아웃 요청)
- Non-blocking DB 접근 (R2DBC, Reactive MongoDB, Redis)

### 4️⃣ 실습 예제
이 프로젝트에는 WebFlux 실습을 위한 `WebFluxDemoController`가 포함되어 있습니다:

- **단일 값 반환 (Mono)**: `/demo/mono`
- **여러 값 스트리밍 (Flux)**: `/demo/flux`
- **비동기 데이터 처리**: `/demo/async/{id}`
- **병렬 처리**: `/demo/parallel`
- **에러 처리**: `/demo/error/{shouldFail}`
- **데이터 변환**: `/demo/transform`

### 5️⃣ 정리
Spring Webflux는 Reactive Streams 기반의 비동기 논블로킹 웹 프레임워크로, 소수의 스레드로도 많은 동시 요청을 처리할 수 있도록 설계되었습니다.
특히 실시간 데이터 처리, API 집약 마이크로서비스, 대규모 트래픽 환경에서 장점이 있습니다. 하지만 단순 CRUD 위주의 서비스에서는 오히려 Spring MVC가 더 단순하고 효율적일 수 있습니다.