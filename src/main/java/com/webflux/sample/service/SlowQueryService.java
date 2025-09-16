package com.webflux.sample.service;

import com.webflux.sample.entity.User;
import com.webflux.sample.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlowQueryService {

    private final UserRepository userRepository;
    private final Random random = new Random();

    /**
     * 모든 사용자를 조회하는 스트리밍 쿼리 - 각 데이터가 준비되는 대로 스트리밍
     */
    public Flux<User> streamAllUsers() {
        log.info("사용자 데이터 스트리밍 시작");
        return userRepository.findAll()
                .delayElements(Duration.ofMillis(100)) // 각 요소마다 지연 추가 (스트리밍 효과 확인용)
                .doOnNext(user -> log.info("스트리밍: 사용자 {} 전송", user.getName()))
                .doOnComplete(() -> log.info("사용자 데이터 스트리밍 완료"));
    }

    /**
     * 페이징을 적용한 사용자 조회
     */
    public Flux<User> findUsersPaged(int page, int size) {
        log.info("페이징된 사용자 조회 시작: 페이지 {}, 크기 {}", page, size);
        return userRepository.findAll()
                .skip((long) page * size)
                .take(size)
                .delayElements(Duration.ofMillis(20)) // 각 요소마다 약간의 지연 추가
                .doOnComplete(() -> log.info("페이징된 사용자 조회 완료"));
    }

    /**
     * 무작위 지연이 있는 사용자 ID 조회
     */
    public Mono<User> findUserByIdWithRandomDelay(Long id) {
        int delay = random.nextInt(500) + 100; // 100~600ms 사이의 무작위 지연
        log.info("사용자 ID {} 조회 시작 (지연: {}ms)", id, delay);
        
        return userRepository.findById(id)
                .delayElement(Duration.ofMillis(delay))
                .doOnSuccess(user -> log.info("사용자 ID {} 조회 완료: {}", id, user != null ? user.getName() : "없음"));
    }
    
    /**
     * 부하 테스트를 위한 대량의 데이터 조회 및 처리
     */
    public Flux<String> processLargeDataSet() {
        log.info("대량 데이터 처리 시작");
        
        return userRepository.findAll()
                .windowTimeout(100, Duration.ofSeconds(1)) // 100개씩 윈도우로 처리
                .flatMap(window -> window
                        .map(user -> {
                            // CPU 부하를 주기 위한 연산
                            String result = user.getName() + " 처리 중...";
                            for (int i = 0; i < 10000; i++) {
                                result = result + i;
                                result = result.substring(0, result.length() > 100 ? 100 : result.length());
                            }
                            return result;
                        })
                        .collectList()
                        .doOnNext(batch -> log.info("배치 {} 항목 처리 완료", batch.size()))
                )
                .flatMapIterable(list -> list)
                .doOnComplete(() -> log.info("대량 데이터 처리 완료"));
    }
}
