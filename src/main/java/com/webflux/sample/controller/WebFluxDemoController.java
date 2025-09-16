package com.webflux.sample.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.extern.slf4j.Slf4j;

/**
 * WebFlux 개념 실습을 위한 데모 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/demo")
public class WebFluxDemoController {

    // 간단한 인메모리 캐시
    private final Map<Integer, String> dataCache = new ConcurrentHashMap<>();

    public WebFluxDemoController() {
        // 캐시 초기화
        IntStream.rangeClosed(1, 10)
                .forEach(i -> dataCache.put(i, "데이터-" + i));
    }

    /**
     * 1. 단일 값 반환 (Mono)
     */
    @GetMapping("/mono")
    public Mono<String> helloMono() {
        log.info("helloMono() 호출됨");
        return Mono.just("Hello WebFlux")
                .doOnNext(data -> log.info("Mono 데이터 전송: {}", data));
    }

    /**
     * 2. 여러 값 스트리밍 (Flux)
     */
    @GetMapping(value = "/flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Long> helloFlux() {
        log.info("helloFlux() 호출됨 - 5초간 1초마다 데이터 스트리밍");
        return Flux.interval(Duration.ofSeconds(1))
                .take(5)
                .doOnNext(data -> log.info("Flux 데이터 전송: {}", data));
    }

    /**
     * 3. 비동기 데이터 처리 예제
     */
    @GetMapping("/async/{id}")
    public Mono<String> asyncData(@PathVariable int id) {
        log.info("asyncData({}) 호출됨", id);
        
        return Mono.fromSupplier(() -> {
            log.info("데이터 조회 시작: ID {}", id);
            // 실제로는 DB 조회 등의 작업이 있을 수 있음
            try {
                Thread.sleep(500); // 데이터 조회 지연 시뮬레이션
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return dataCache.getOrDefault(id, "데이터 없음");
        })
        .doOnNext(data -> log.info("데이터 조회 완료: {}", data));
    }

    /**
     * 4. 병렬 처리 예제
     */
    @GetMapping("/parallel")
    public Mono<List<String>> parallelProcessing() {
        log.info("parallelProcessing() 호출됨");
        
        return Flux.range(1, 5)
                .parallel() // 병렬 처리
                .runOn(reactor.core.scheduler.Schedulers.parallel())
                .flatMap(i -> {
                    log.info("병렬 처리 중: {}, 스레드: {}", i, Thread.currentThread().getName());
                    return Mono.just("처리된 데이터 " + i)
                            .delayElement(Duration.ofMillis(i * 100)); // 다양한 처리 시간 시뮬레이션
                })
                .sequential() // 결과를 다시 순차적으로 모음
                .collectList()
                .doOnNext(list -> log.info("병렬 처리 완료: {} 항목", list.size()));
    }

    /**
     * 5. 에러 처리 예제
     */
    @GetMapping("/error/{shouldFail}")
    public Mono<String> errorHandling(@PathVariable boolean shouldFail) {
        log.info("errorHandling({}) 호출됨", shouldFail);
        
        return Mono.defer(() -> {
            if (shouldFail) {
                log.info("에러 발생 시뮬레이션");
                return Mono.error(new RuntimeException("의도적인 에러 발생"));
            } else {
                return Mono.just("정상 처리 완료");
            }
        })
        .onErrorResume(e -> {
            log.error("에러 처리: {}", e.getMessage());
            return Mono.just("에러가 발생했지만 복구됨: " + e.getMessage());
        })
        .doOnNext(data -> log.info("최종 결과: {}", data));
    }

    /**
     * 6. 데이터 변환 예제
     */
    @GetMapping("/transform")
    public Flux<Map<String, Object>> transformData() {
        log.info("transformData() 호출됨");
        
        return Flux.range(1, 5)
                .map(i -> {
                    Map<String, Object> result = new ConcurrentHashMap<>();
                    result.put("id", i);
                    result.put("value", "데이터-" + i);
                    result.put("squared", i * i);
                    log.info("데이터 변환: ID {}", i);
                    return result;
                })
                .doOnComplete(() -> log.info("데이터 변환 완료"));
    }
}
