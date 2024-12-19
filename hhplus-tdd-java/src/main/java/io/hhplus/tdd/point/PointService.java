package io.hhplus.tdd.point;

import io.hhplus.tdd.CustomException;
import io.hhplus.tdd.ErrorCode;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class PointService {

    private static final long MAX_POINT_LIMIT = 1000000;

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;
    private final ConcurrentHashMap<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();  // 사용자 ID별로 ReentrantLock 관리

    public UserPoint chargePoints(long id, long chargeAmount) {

        chargeValidations(id, chargeAmount);

        // 사용자 ID별로 락 생성 또는 가져오기
        lockMap.putIfAbsent(id, new ReentrantLock());
        ReentrantLock lock = lockMap.get(id);
        lock.lock();

        try{
            long prevPoint = 0;
            UserPoint userPoint = userPointTable.selectById(id);
            if (userPoint != null){
                prevPoint = userPoint.point();
            }
            UserPoint result = userPointTable.insertOrUpdate(id, chargeAmount+prevPoint);
            pointHistoryTable.insert(id, chargeAmount, TransactionType.CHARGE, System.currentTimeMillis());
            return result;
        } finally {
            lock.unlock();
        }
    }

    private static void chargeValidations(long id, long chargeAmount) {

        if (id <= 0) {
            throw new CustomException(ErrorCode.INVALID_USER_ID);
        }

        if (chargeAmount <= 0) {
            throw new CustomException(ErrorCode.INVALID_POINT_INPUT);
        }

        if (chargeAmount > MAX_POINT_LIMIT) {
            throw new CustomException(ErrorCode.INPUT_POINT_EXCEEDED);
        }
    }


    public UserPoint getPointByUser(long id) {
        if (id <= 0) {
            throw new CustomException(ErrorCode.INVALID_USER_ID);
        }

        UserPoint user = userPointTable.selectById(id);
        if (user == null){
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    public List<PointHistory> getPointHistoriesByUser(long id) {
        if (id <= 0) {
            throw new CustomException(ErrorCode.INVALID_USER_ID);
        }

        UserPoint user = userPointTable.selectById(id);
        if (user == null){
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        return pointHistoryTable.selectAllByUserId(id);
    }

    public UserPoint usePoints(long id, long useAmount) {

        usePointsValidations(id, useAmount);

        lockMap.putIfAbsent(id, new ReentrantLock());
        ReentrantLock lock = lockMap.get(id);
        lock.lock();

        try{
            UserPoint userPoint = userPointTable.selectById(id);
            if (userPoint == null){
                throw new CustomException(ErrorCode.USER_NOT_FOUND);
            }

            long nowPoint = userPoint.point();
            if (nowPoint < useAmount) {
                throw new CustomException(ErrorCode.POINT_INSUFFICIENT);
            }
            UserPoint result = userPointTable.insertOrUpdate(id, nowPoint-useAmount);
            pointHistoryTable.insert(id, useAmount, TransactionType.USE, System.currentTimeMillis());
            return result;
        }finally {
            lock.unlock();
        }
    }

    private void usePointsValidations(long id, long useAmount) {
        if (id <= 0) {
            throw new CustomException(ErrorCode.INVALID_USER_ID);
        }

        if (useAmount <= 0) {
            throw new CustomException(ErrorCode.INVALID_POINT_INPUT);
        }
    }
}
