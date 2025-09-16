package com.webflux.sample.config;

import com.webflux.sample.entity.User;
import com.webflux.sample.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader {

    private final UserRepository userRepository;
    private final Random random = new Random();

    @EventListener(ApplicationReadyEvent.class)
    public void loadData() {
        log.info("시작: 더미 데이터 생성");
        
        // 데이터베이스에 이미 있는 사용자 수 확인
        userRepository.count()
            .flatMap(count -> {
                log.info("현재 데이터베이스에 {} 명의 사용자가 있습니다.", count);
                
                if (count > 0) {
                    log.info("이미 데이터가 존재하므로 더미 데이터 생성을 건너뜁니다.");
                    return Mono.empty();
                }
                
                // H2 메모리 데이터베이스에 적합한 양의 더미 데이터 생성 (1,000명)
                int batchSize = 100;
                int totalUsers = 1000;
                
                log.info("총 {}명의 더미 사용자 데이터를 {}개씩 배치로 생성합니다.", totalUsers, batchSize);
                
                return Flux.range(0, totalUsers / batchSize)
                    .concatMap(batch -> {
                        List<User> users = new ArrayList<>();
                        int startIdx = batch * batchSize;
                        
                        for (int i = startIdx; i < startIdx + batchSize; i++) {
                            User user = User.builder()
                                .name("사용자_" + i)
                                .email("user" + i + "@example.com")
                                .build();
                            users.add(user);
                        }
                        
                        log.info("배치 {} 생성 중: {} ~ {}", batch, startIdx, startIdx + batchSize - 1);
                        return userRepository.saveAll(users)
                                .doOnNext(user -> {
                                    if (user.getId() != null && user.getId() % 100 == 0) {
                                        log.info("사용자 {} 생성됨", user.getId());
                                    }
                                });
                    })
                    .collectList()
                    .doOnSuccess(savedUsers -> 
                        log.info("완료: {} 명의 더미 사용자 데이터가 생성되었습니다.", savedUsers.size()));
            })
            .subscribe(result -> {
                log.info("데이터 로딩 완료");
            }, error -> {
                log.error("데이터 로딩 중 오류 발생: {}", error.getMessage());
            });
    }
    
    // 부하 테스트를 위한 지연 메서드
    private Mono<Void> randomDelay() {
        return Mono.delay(Duration.ofMillis(random.nextInt(50) + 10)).then();
    }
}
