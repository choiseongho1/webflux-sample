package com.webflux.sample.controller;

import com.webflux.sample.entity.User;
import com.webflux.sample.repository.UserRepository;
import com.webflux.sample.service.SlowQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping("/api/stream")
@RequiredArgsConstructor
public class StreamingController {

    private final UserRepository userRepository;
    private final SlowQueryService slowQueryService;

    /**
     * Server-Sent Events(SSE)를 사용한 사용자 데이터 스트리밍
     * 클라이언트는 연결을 유지하면서 서버에서 데이터가 준비되는 대로 수신
     */
    @GetMapping(value = "/users", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<User> streamUsers() {
        log.info("SSE 스트리밍 시작: 사용자 데이터");
        return slowQueryService.streamAllUsers();
    }

    /**
     * 무한 스트리밍 예제 - 시간 정보를 1초마다 지속적으로 전송
     */
    @GetMapping(value = "/time", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamTime() {
        log.info("SSE 스트리밍 시작: 시간 데이터");
        return Flux.fromStream(Stream.generate(() -> LocalDateTime.now().toString()))
                .delayElements(Duration.ofSeconds(1))
                .doOnNext(time -> log.info("스트리밍: 현재 시간 {} 전송", time));
    }

    /**
     * 대량의 사용자 데이터를 청크 단위로 스트리밍
     */
    @GetMapping(value = "/users/chunked", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamUsersInChunks() {
        log.info("SSE 스트리밍 시작: 사용자 데이터 청크");
        return userRepository.findAll()
                .buffer(10)  // 10개씩 그룹화
                .delayElements(Duration.ofMillis(500))  // 각 청크마다 지연
                .map(users -> {
                    StringBuilder chunk = new StringBuilder("사용자 청크: ");
                    users.forEach(user -> chunk.append(user.getName()).append(", "));
                    log.info("스트리밍: 청크 전송 - {} 사용자", users.size());
                    return chunk.toString();
                })
                .doOnComplete(() -> log.info("SSE 스트리밍 완료: 사용자 데이터 청크"));
    }
}
