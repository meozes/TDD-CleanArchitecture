package io.hhplus.tdd.point;

import io.hhplus.tdd.CustomException;
import io.hhplus.tdd.ErrorCode;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private static final long MAX_POINT_LIMIT = 1000000;

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public UserPoint chargePoints(long id, long chargeAmount) {

        long prevPoint = 0;
        chargeValidations(id, chargeAmount);

        UserPoint userPoint = userPointTable.selectById(id);
        if (userPoint != null){
            prevPoint = userPoint.point();
        }

        UserPoint result = userPointTable.insertOrUpdate(id, chargeAmount+prevPoint);
        pointHistoryTable.insert(id, chargeAmount, TransactionType.CHARGE, System.currentTimeMillis());
        return result;
    }

    private static void chargeValidations(long id, long amount) {

        if (id <= 0) {
            throw new CustomException(ErrorCode.INVALID_USER_ID);
        }

        if (amount <= 0) {
            throw new CustomException(ErrorCode.INVALID_POINT_INPUT);
        }

        if (amount > MAX_POINT_LIMIT) {
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

    public UserPoint usePoints(long id, long amount) {

        usePointsValidations(id, amount);

        UserPoint userPoint = userPointTable.selectById(id);
        if (userPoint == null){
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        long nowPoint = userPoint.point();
        if (nowPoint < amount) {
            throw new CustomException(ErrorCode.POINT_INSUFFICIENT);
        }

        UserPoint result = userPointTable.insertOrUpdate(id, nowPoint-amount);
        pointHistoryTable.insert(id, amount, TransactionType.USE, System.currentTimeMillis());
        return result;
    }

    private void usePointsValidations(long id, long amount) {
        if (id <= 0) {
            throw new CustomException(ErrorCode.INVALID_USER_ID);
        }

        if (amount <= 0) {
            throw new CustomException(ErrorCode.INVALID_POINT_INPUT);
        }
    }
}
