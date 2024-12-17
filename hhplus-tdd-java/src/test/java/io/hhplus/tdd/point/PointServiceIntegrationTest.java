package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest // 통합테스트 시 사용
public class PointServiceIntegrationTest {     // 다수의 스레드를 생성하여 통합 테스트를 진행한다.

    @Autowired
    private PointService pointService;

    @Autowired
    private UserPointTable userPointTable;


    @Test
    public void 포인트_충전_통합_성공() throws InterruptedException { // '여러 스레드'가 포인트 '충전' 시 동시성 제어 테스트
        // 준비
        long id = 1L;
        long originAmount = 20000L;
        pointService.chargePoints(id, originAmount);

        int threadCount = 10;
        long chargeAmountPerThread = 1000L;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount); // 고정된 개수의 스레드 풀 생성
        CountDownLatch countDownLatch = new CountDownLatch(threadCount); // 여러개의 스레드가 특정 조건 만족할 때까지 기다림

        long startTime = System.currentTimeMillis();

        // 실행
        for(int i=0; i<threadCount; i++){
            executorService.submit(() -> {
                try{
                    pointService.chargePoints(id, chargeAmountPerThread); // 작업 수행
                }catch(Exception e){
                    System.out.println(e.getMessage());
                }finally{
                    countDownLatch.countDown(); // 호출할때마다 카운트 1씩 감소
                }
            });
        }

        countDownLatch.await(); // 카운트가 0이 될때가지 현재 스레드 block. 모든 스레드 완료까지 대기
        executorService.shutdown(); // 스레드 풀 종 및 자원 해제
        executorService.awaitTermination(1, TimeUnit.MINUTES); // 특정 시간동안 스레드 풀 종료되기를 기다림

        long endTime = System.currentTimeMillis(); // 종료 시간 기록

        // 검증
        UserPoint userPoint = userPointTable.selectById(id);
        assertEquals(userPoint.point(), 30000L);

        System.out.println("Test completed in " + (endTime - startTime) + " ms");
    }

    @Test
    public void 포인트_사용_통합_성공() throws InterruptedException { // '여러 스레드'가 포인트 '사용' 시 동시성 제어 테스트
        // 준비
        long id = 2L;
        long originAmount = 50000L;
        pointService.chargePoints(id, originAmount);

        int threadCount = 10;
        long useAmountPerThread = 1000L;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount); // 고정된 개수의 스레드 풀 생성
        CountDownLatch countDownLatch = new CountDownLatch(threadCount); // 여러개의 스레드가 특정 조건 만족할 때까지 기다림

        long startTime = System.currentTimeMillis();

        // 실행
        for(int i=0; i<threadCount; i++){
            executorService.submit(() -> {
                        try{
                            pointService.usePoints(id, useAmountPerThread); // 작업 수행
                        }catch(Exception e){
                            System.out.println(e.getMessage());
                        }finally{
                            countDownLatch.countDown(); // 호출할때마다 카운트 1씩 감소
                        }
                    });
        }
        countDownLatch.await(); // 카운트가 0이 될때가지 현재 스레드 block. 모든 스레드 완료까지 대기
        executorService.shutdown(); // 스레드 풀 종 및 자원 해제
        executorService.awaitTermination(1, TimeUnit.MINUTES); // 특정 시간동안 스레드 풀 종료되기를 기다림

        long endTime = System.currentTimeMillis(); // 종료 시간 기록

        // 검증
        UserPoint userPoint = userPointTable.selectById(id);
        assertEquals(userPoint.point(), 40000L);

        System.out.println("Test completed in " + (endTime - startTime) + " ms");

    }

}
