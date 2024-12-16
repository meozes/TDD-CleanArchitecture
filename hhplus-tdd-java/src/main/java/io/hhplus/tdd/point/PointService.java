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

    UserPointTable userPointTable;
    PointHistoryTable pointHistoryTable;

    public UserPoint chargePoints(long id, long amount) {

        if (amount <= 0) {
            throw new CustomException(ErrorCode.INVALID_POINT_INPUT);
        }

        UserPoint userPoint = userPointTable.selectById(id);
        if (userPoint != null) { //신규가 아니면
            long prevPoint = userPoint.point();
            amount += prevPoint;
        }

        pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());

        return userPointTable.insertOrUpdate(id, amount);
    }


}
