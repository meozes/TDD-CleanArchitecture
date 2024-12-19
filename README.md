# 💡 동시성

## 동시성(Concurrency)

동시성은 프로세스 관점에서 각 작업을 스레드를 통해 빠르게 전환하며 동시에 실행되는 것처럼 보이는 방식이다. 이때, 여러 스레드가 공유 자원에 접근하려 할 때 문제가 발생할 수 있다.

## 병렬성(Parallelism)

병렬성은 멀티 스레드와 멀티 프로세서를 이용해 여러 작업을 실제로 동시에 수행하는 것이다.  
- **동기**는 "같은 시간에 실행될 수 있는 작업을 나누는 것"이며,  
- **병렬성**은 "여러 작업이 실제로 동시에 실행되는 것"이다.

---

# 💡 동시성 문제

동시성 문제는 여러 스레드가 동일한 자원에 동시에 접근할 때 발생한다. 멀티스레딩 환경에서 여러 스레드가 동시에 실행되므로, 동일 자원에 대한 경합(Race Condition), 교착 상태(Deadlock), 기아 상태(Starvation) 등의 문제가 발생할 수 있다.

### 주요 문제
- **경쟁 상태(Race Condition)**: 여러 스레드가 동시에 같은 자원에 접근하여 그 값을 변경할 때 발생하는 문제이다.
- **교착 상태(Deadlock)**: 두 개 이상의 스레드가 서로 자원을 기다리며 무한 대기 상태에 빠지는 것이다.
- **기아 상태(Starvation)**: 일부 스레드가 자원을 계속 기다리다가 실행되지 못하는 상태이다.

---

# 💡 동시성 문제 해결 방법

## 1. 애플리케이션 레벨에서 해결
- **Synchronized와 @Transactional**  
- **ReentrantLock**과 **ConcurrentHashMap**

## 2. 데이터베이스에서 해결
- **낙관적 락(Optimistic Locking)**: 락을 사용하지 않고 버전 컬럼을 추가해 데이터의 정합성을 맞춘다. 동시성 이슈가 발생하지 않을 것으로 예상하고 모든 요청을 락 없이 처리하며, 문제가 발생하면 롤백한다.
- **비관적 락(Pessimistic Locking)**: 동시성 이슈가 자주 발생할 것이라 예상하여 락을 사용한다. 한 트랜잭션이 데이터에 접근하면 다른 트랜잭션은 해당 데이터를 읽거나 쓸 수 없다. `SELECT ~ FOR UPDATE` SQL문을 사용해 구현한다.

## 3. Redis
Redis를 사용한 분산 락도 동시성 문제 해결에 유용하다.
<br>

---

# 📍 애플리케이션 레벨 해결

## 1. Synchronized와 @Transactional

**@Transactional**은 데이터베이스 트랜잭션을 관리하며, 메소드 내에서 발생하는 모든 데이터베이스 작업들을 하나의 트랜잭션으로 묶는다.  
**Synchronized**는 자바에서 동기화를 위해 사용되며, 한 번에 하나의 스레드만 특정 코드 블록에 접근할 수 있도록 제한한다.

```java
public synchronized void syncMethod() {
    // Critical section
    System.out.println("Thread-safe method");
}
```
이때, **메서드 전체에 락이 걸려** 한 스레드가 실행 중일 때 다른 스레드는 대기해야 한다.

## 문제점과 한계
- 트랜잭션이 종료되기 전에 다른 스레드가 동기화된 메소드에 접근할 수 있어, 예기치 않은 상태가 발생할 수 있다.
- **Synchronized**는 동일 프로세스 내에서만 동시성 제어가 가능하며, 서버가 여러 대일 경우 자원 접근을 제어할 수 없다.

## 2. ReentrantLock
**ReentrantLock**은 락을 명시적으로 관리할 수 있는 클래스이다. 동기화 블록을 시작할 때 락을 획득하고, 끝날 때 락을 해제하는 방식으로 동시성을 제어한다.

### 예시 코드
```java
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockExample {
    private final Lock lock = new ReentrantLock();

    public void performTask() {
        lock.lock(); // 락 획득
        try {
            // Critical section
            System.out.println("Thread-safe using Lock");
        } finally {
            lock.unlock(); // 락 해제
        }
    }
}
```
**ReentrantLock**과 **ConcurrentHashMap**을 사용하여 특정 사용자 ID에 대해 락을 관리할 수 있다. 이 방식은 높은 동시성을 보장하면서도 성능 최적화에 유리하다.
* **ConcurrentHashMap**은 키(id)별로 동시 작업을 처리. 각 키가 서로 다른 데이터를 나타내므로, A, B, C 세 사람이 동시에 자신의 데이터를 읽거나 쓰는 작업을 할 때 충돌하지 않는다.
* **ReentrantLock**은 특정 사용자(A)에 대해 동기화된 작업(충전 -> 사용)을 보장한다.

### 예시 코드
```java
private final ConcurrentHashMap<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();

public UserPoint chargePoints(long id, long chargeAmount) {

    chargeValidations(id, chargeAmount);

    // 사용자 ID별로 락 생성 또는 가져오기
    lockMap.putIfAbsent(id, new ReentrantLock());
    ReentrantLock lock = lockMap.get(id);
    lock.lock();

    try {
        long prevPoint = 0;
        UserPoint userPoint = userPointTable.selectById(id);
        if (userPoint != null){
            prevPoint = userPoint.point();
        }
        UserPoint result = userPointTable.insertOrUpdate(id, chargeAmount + prevPoint);
        pointHistoryTable.insert(id, chargeAmount, TransactionType.CHARGE, System.currentTimeMillis());
        return result;
    } finally {
        lock.unlock();
    }
}
```

---

### 🌀 동시성 문제 해결에 대한 이해의 필요성
실무에서는 Spring, Hibernate 같은 프레임워크에서 동시성 제어와 데이터 동기화를 대부분 처리해주어(트랜잭션 관리, 분산 락, 캐시 솔루션 등) 직접적인 락 관리가 필요 없는 경우가 많지만,
Spring이나 Hibernate 기반 시스템이 동작하더라도, 내부적으로 ReentrantLock, ConcurrentHashMap, 또는 다른 동시성 도구를 활용하기 때문에 이런 동작들이 어떻게 구현되어 있는지 알아야한다.







