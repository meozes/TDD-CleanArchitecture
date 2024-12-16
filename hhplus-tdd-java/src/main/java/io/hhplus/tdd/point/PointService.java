package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PointService {

    UserPointTable userPointTable;

    public UserPoint chargePoints(long id, long amount) {

        if (amount < 0) {
            throw new IllegalArgumentException("포인트는 음수 값이 될 수 없습니다.");
        }
        return userPointTable.insertOrUpdate(id, amount);
    }
}
