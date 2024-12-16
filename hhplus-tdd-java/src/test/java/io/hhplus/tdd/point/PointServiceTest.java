package io.hhplus.tdd.point;


import io.hhplus.tdd.CustomException;
import io.hhplus.tdd.ErrorCode;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@SpringBootTest
public class PointServiceTest {

    @InjectMocks
    private PointService pointService;

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pointService = new PointService(userPointTable, pointHistoryTable);
    }

    @Test
    public void 회원_불가_id() {
        // given
        long userId = -1;
        long amount = 10000;

        // When & Then
        CustomException e = assertThrows(
                CustomException.class,
                () -> pointService.chargePoints(userId, amount)
        );

        assertEquals(
                ErrorCode.INVALID_USER_ID.getCode(), e.getErrorCode().getCode()
        );
    }

    @Test
    public void 충전_불가_포인트() {

        // given
        long userId = 1;
        long amount = -10000;

        // When & Then
        CustomException e = assertThrows(
                CustomException.class,
                () -> pointService.chargePoints(userId, amount)
        );

        assertEquals(
                ErrorCode.INVALID_POINT_INPUT.getCode(), e.getErrorCode().getCode()
        );

    }

    @Test
    public void 신규_회원_포인트_충전() {
        // given
        long userId = 1;
        long amount = 10000;

        UserPoint userPoint = new UserPoint(userId, amount, System.currentTimeMillis());

        // 유저가 존재하지 않는 경우 신규 등록
        given(userPointTable.selectById(anyLong())).willReturn(null); // 무조건 null 리턴하여 존재하지 않는 경우 만든다.
        given(userPointTable.insertOrUpdate(anyLong(), anyLong())).willReturn(userPoint);

        // when
        UserPoint result = pointService.chargePoints(userId, amount);

        // then
        assertThat(result.id()).isEqualTo(userPoint.id());
        assertThat(result.point()).isEqualTo(userPoint.point());
    }

    @Test
    public void 기존_회원_포인트_충전() {
        // given
        long userId = 2;
        long prevAmount = 20000;
        long chargeAmount = 30000;

        UserPoint userPoint = new UserPoint(userId, prevAmount, System.currentTimeMillis()); // 기존 회원
        given(userPointTable.selectById(userId)).willReturn(userPoint);
        given(userPointTable.insertOrUpdate(anyLong(), anyLong())).willReturn(new UserPoint(userId, prevAmount+chargeAmount, System.currentTimeMillis())); //추가 충전

        UserPoint result = pointService.chargePoints(userId, prevAmount+chargeAmount);

        // then
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.point()).isEqualTo(prevAmount+chargeAmount);

    }


}
