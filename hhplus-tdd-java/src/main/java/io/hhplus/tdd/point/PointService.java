package io.hhplus.tdd.point;

import io.hhplus.tdd.CustomException;
import io.hhplus.tdd.ErrorCode;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PointService {

    private static final long MAX_POINT_LIMIT = 2147483647;

    UserPointTable userPointTable;
    PointHistoryTable pointHistoryTable;

    public UserPoint chargePoints(long id, long amount) {

        chargeValidations(id, amount);

        UserPoint userPoint = userPointTable.selectById(id);
        long newPoint = userPoint.point();

        if (userPoint != null) { //신규가 아니면
            long prevPoint = userPoint.point();
            amount += prevPoint;

            if (newPoint > MAX_POINT_LIMIT) {
                throw new CustomException(ErrorCode.POINT_EXCEEDED);
            }
        }

        UserPoint result = userPointTable.insertOrUpdate(id, amount);
        pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());
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
            throw new CustomException(ErrorCode.POINT_EXCEEDED);
        }
    }


}
