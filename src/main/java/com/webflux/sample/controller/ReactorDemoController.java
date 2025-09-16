package com.webflux.sample.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Slf4j
@RestController
@RequestMapping("/reactor")
public class ReactorDemoController {

    private final Random random = new Random();
    private final List<String> dataList = Arrays.asList(
            "데이터1", "데이터2", "데이터3", "데이터4", "데이터5",
            "데이터6", "데이터7", "데이터8", "데이터9", "데이터10"
    );

    /**
     * 1. Mono 기본 예제 - 단일 값 반환
     */
    @GetMapping("/mono/basic")
    public Mono<String> monoBasic() {
        log.info("monoBasic() 호출됨");
        return Mono.just("Mono 기본 예제: 단일 값 반환")
                .doOnSubscribe(s -> log.info("구독 시작"))
                .doOnNext(data -> log.info("데이터 처리: {}", data))
                .doOnSuccess(data -> log.info("처리 완료"));
    }

    /**
     * 2. Mono 지연 처리 예제
     */
    @GetMapping("/mono/delay")
    public Mono<String> monoDelay() {
        log.info("monoDelay() 호출됨");
        return Mono.just("Mono 지연 처리 예제")
                .delayElement(Duration.ofSeconds(2))
                .doOnSubscribe(s -> log.info("구독 시작 - 2초 후 데이터 전달"))
                .doOnNext(data -> log.info("데이터 처리: {}", data));
    }

    /**
     * 3. Mono 에러 처리 예제
     */
    @GetMapping("/mono/error")
    public Mono<String> monoError() {
        log.info("monoError() 호출됨");
        return Mono.just("데이터")
                .flatMap(data -> {
                    if (random.nextBoolean()) {
                        log.info("에러 발생 시뮬레이션");
                        return Mono.error(new RuntimeException("의도적인 에러 발생"));
                    }
                    return Mono.just(data);
                })
                .onErrorResume(e -> {
                    log.error("에러 처리: {}", e.getMessage());
                    return Mono.just("에러가 발생했지만 복구됨: " + e.getMessage());
                })
                .doOnSuccess(data -> log.info("처리 완료: {}", data));
    }

    /**
     * 4. Flux 기본 예제 - 여러 값 반환
     */
    @GetMapping("/flux/basic")
    public Flux<String> fluxBasic() {
        log.info("fluxBasic() 호출됨");
        return Flux.fromIterable(dataList)
                .doOnSubscribe(s -> log.info("구독 시작"))
                .doOnNext(data -> log.info("데이터 처리: {}", data))
                .doOnComplete(() -> log.info("모든 데이터 처리 완료"));
    }

    /**
     * 5. Flux 스트리밍 예제 (SSE)
     */
    @GetMapping(value = "/flux/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> fluxStream() {
        log.info("fluxStream() 호출됨");
        return Flux.fromIterable(dataList)
                .delayElements(Duration.ofSeconds(1))
                .doOnSubscribe(s -> log.info("스트리밍 구독 시작 - 1초 간격으로 데이터 전송"))
                .doOnNext(data -> log.info("스트리밍 데이터 전송: {}", data))
                .doOnComplete(() -> log.info("스트리밍 완료"));
    }

    /**
     * 6. Flux 변환 예제 (map, filter)
     */
    @GetMapping("/flux/transform")
    public Flux<String> fluxTransform() {
        log.info("fluxTransform() 호출됨");
        return Flux.fromIterable(dataList)
                .map(data -> data + " 변환됨")
                .filter(data -> data.length() > 7)
                .doOnNext(data -> log.info("변환 후 데이터: {}", data))
                .doOnComplete(() -> log.info("변환 처리 완료"));
    }

    /**
     * 7. Flux 병합 예제 (zip)
     */
    @GetMapping("/flux/zip")
    public Flux<String> fluxZip() {
        log.info("fluxZip() 호출됨");
        
        Flux<String> flux1 = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));
        
        Flux<Integer> flux2 = Flux.just(1, 2, 3)
                .delayElements(Duration.ofMillis(200));
        
        return Flux.zip(flux1, flux2, (s, i) -> s + i)
                .doOnNext(data -> log.info("병합된 데이터: {}", data))
                .doOnComplete(() -> log.info("병합 처리 완료"));
    }

    /**
     * 8. Backpressure 예제
     */
    @GetMapping(value = "/flux/backpressure", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> fluxBackpressure() {
        log.info("fluxBackpressure() 호출됨");
        
        return Flux.range(1, 100)
                .map(i -> "데이터 " + i)
                // 백프레셔 전략 적용 - DROP: 버퍼가 가득 차면 새로운 항목을 버림
                .onBackpressureDrop(dropped -> log.info("백프레셔로 인해 버려진 항목: {}", dropped))
                .publishOn(Schedulers.boundedElastic(), 10) // 버퍼 크기 10으로 제한
                .doOnRequest(n -> log.info("요청된 항목 수: {}", n))
                .delayElements(Duration.ofMillis(100)) // 생산자 속도 제어
                .doOnNext(data -> {
                    log.info("백프레셔 적용 데이터 처리: {}", data);
                    try {
                        // 소비자가 처리하는데 시간이 걸리는 상황 시뮬레이션
                        Thread.sleep(200); // 소비자가 생산자보다 느림
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                })
                .doOnComplete(() -> log.info("백프레셔 예제 완료"));
    }

    /**
     * 9. Mono와 Flux 변환 예제
     */
    @GetMapping("/convert")
    public Mono<List<String>> convertFluxToMono() {
        log.info("convertFluxToMono() 호출됨");
        
        // Flux -> Mono<List> 변환
        return Flux.fromIterable(dataList)
                .collectList()
                .doOnSuccess(list -> log.info("Flux에서 Mono<List>로 변환 완료: {} 항목", list.size()));
    }

    /**
     * 10. 비동기 처리 예제
     */
    @GetMapping("/async")
    public Mono<String> asyncProcessing() {
        log.info("asyncProcessing() 호출됨");
        
        return Mono.fromCallable(() -> {
                    log.info("비동기 작업 시작 (다른 스레드에서 실행): {}", Thread.currentThread().getName());
                    // 시간이 걸리는 작업 시뮬레이션
                    Thread.sleep(1000);
                    return "비동기 작업 결과";
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(result -> log.info("비동기 작업 완료: {}", result));
    }
    
    /**
     * 11. 백프레셔 전략 비교 예제
     */
    @GetMapping("/backpressure/strategies")
    public Mono<String> backpressureStrategies() {
        log.info("백프레셔 전략 비교 예제 시작");
        
        // 1. DROP 전략 - 버퍼가 가득 차면 새로운 항목을 버림
        Flux.interval(Duration.ofMillis(1))
                .take(1000)
                .onBackpressureDrop(i -> log.info("DROP 전략: 항목 {} 버려짐", i))
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(i -> {
                    if (i % 100 == 0) log.info("DROP 전략 처리: {}", i);
                    try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                })
                .subscribe();
        
        // 2. LATEST 전략 - 버퍼가 가득 차면 최신 항목만 유지
        Flux.interval(Duration.ofMillis(1))
                .take(1000)
                .onBackpressureLatest()
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(i -> {
                    if (i % 100 == 0) log.info("LATEST 전략 처리: {}", i);
                    try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                })
                .subscribe();
        
        // 3. ERROR 전략 - 버퍼가 가득 차면 에러 발생
        Flux.interval(Duration.ofMillis(1))
                .take(1000)
                .onBackpressureError()
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(i -> {
                    if (i % 100 == 0) log.info("ERROR 전략 처리: {}", i);
                    try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                })
                .subscribe(null, e -> log.error("ERROR 전략에서 에러 발생: {}", e.getMessage()));
        
        // 4. BUFFER 전략 - 무제한 버퍼 사용 (메모리 주의)
        Flux.interval(Duration.ofMillis(1))
                .take(1000)
                .onBackpressureBuffer()
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(i -> {
                    if (i % 100 == 0) log.info("BUFFER 전략 처리: {}", i);
                    try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                })
                .subscribe();
        
        return Mono.just("백프레셔 전략 비교가 백그라운드에서 실행 중입니다. 로그를 확인하세요.");
    }
}
