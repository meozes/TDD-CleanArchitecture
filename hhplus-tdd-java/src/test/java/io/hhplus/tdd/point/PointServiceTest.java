package io.hhplus.tdd.point;


import io.hhplus.tdd.CustomException;
import io.hhplus.tdd.ErrorCode;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.InjectMocks;
import org.mockito.Mock;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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
    public void 유효하지_않은_id() {
        // 준비
        long userId = -1L;
        long amount = 10000L;

        // 실행 & 검증
        CustomException e = assertThrows(
                CustomException.class,
                () -> pointService.chargePoints(userId, amount)
        );

        assertEquals(ErrorCode.INVALID_USER_ID.getCode(), e.getErrorCode().getCode());

        // 실패 시 다음 로직 수행되면 안된다.
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(), anyLong());
    }

    @Test
    public void 존재하지_않는_회원_조회() {
        // 준비
        long id = 100L;

        // 실행
        given(userPointTable.selectById(id)).willReturn(null);

        //검증
        CustomException e = assertThrows(
                CustomException.class,
                () -> pointService.getPointByUser(id)
        );

        assertEquals(ErrorCode.USER_NOT_FOUND.getCode(), e.getErrorCode().getCode());
    }

    @Test
    public void 충전_불가_포인트() {

        // 준비
        long userId = 1L;
        long amount = -10000L;

        // 실행 & 검증
        CustomException e = assertThrows(
                CustomException.class,
                () -> pointService.chargePoints(userId, amount)
        );

        assertEquals(ErrorCode.INVALID_POINT_INPUT.getCode(), e.getErrorCode().getCode());

        // 실패 시 다음 로직 수행되면 안된다.
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(), anyLong());


        // 준비
        long userId2 = 2L;
        long amount2 = 9000000L;

        // 실행 & 검증
        CustomException e2 = assertThrows(
                CustomException.class,
                () -> pointService.chargePoints(userId2, amount2)
        );

        assertEquals(ErrorCode.INPUT_POINT_EXCEEDED.getCode(), e2.getErrorCode().getCode());

        // 실패 시 다음 로직 수행되면 안된다.
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(), anyLong());

    }

    @Test
    public void 신규_회원_포인트_충전() {
        // 준비
        long userId = 1L;
        long amount = 10000L;

        UserPoint userPoint = new UserPoint(userId, amount, System.currentTimeMillis());

        // 유저가 존재하지 않는 경우 신규 등록
        given(userPointTable.selectById(anyLong())).willReturn(null);
        given(userPointTable.insertOrUpdate(anyLong(), anyLong())).willReturn(userPoint);

        // 실행
        UserPoint result = pointService.chargePoints(userId, amount);

        // 검증
        assertEquals(result.id(), userPoint.id());
        assertEquals(result.point(), userPoint.point());

        verify(pointHistoryTable, times(1))
                .insert(eq(userId), eq(amount), eq(TransactionType.CHARGE), anyLong()); //이력 저장여부 확인
    }

    @Test
    public void 기존_회원_포인트_충전() {
        // 준비
        long userId = 2L;
        long prevAmount = 20000L;
        long chargeAmount = 30000L;
        long expectedTotal = prevAmount + chargeAmount;

        UserPoint userPoint = new UserPoint(userId, prevAmount, System.currentTimeMillis());
        given(userPointTable.selectById(userId)).willReturn(userPoint);
        given(userPointTable.insertOrUpdate(anyLong(), anyLong())).willReturn(new UserPoint(userId, expectedTotal, System.currentTimeMillis()));

        // 실행
        UserPoint result = pointService.chargePoints(userId, chargeAmount);

        // 검증
        assertEquals(result.id(), userId);
        assertEquals(result.point(), expectedTotal);

        verify(pointHistoryTable, times(1))
                .insert(eq(userId), eq(chargeAmount), eq(TransactionType.CHARGE), anyLong());
    }


    @Test
    public void 잔고_초과_사용() {

        // 준비
        long id = 1L;
        long amount = 10000L; //기존잔고

        UserPoint userPoint = new UserPoint(id, amount, System.currentTimeMillis());
        given(userPointTable.selectById(id)).willReturn(userPoint);

        // 실행 & 검증
        CustomException e = assertThrows(
                CustomException.class,
                () -> pointService.usePoints(id, 50000L)
        );

        assertEquals(ErrorCode.POINT_INSUFFICIENT.getCode(), e.getErrorCode().getCode());

        // 실패 시 다음 로직 수행되면 안된다.
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(), anyLong());
    }

    @Test
    public void 포인트_사용() {
        // 준비
        long id = 1L;
        long prevAmount = 50000L; //기존잔고
        long useAmount = 10000L;

        UserPoint userPoint = new UserPoint(id, prevAmount, System.currentTimeMillis());
        given(userPointTable.selectById(id)).willReturn(userPoint);
        given(userPointTable.insertOrUpdate(anyLong(), anyLong())).willReturn(new UserPoint(id, prevAmount-useAmount, System.currentTimeMillis()));

        // 실행
        UserPoint result = pointService.usePoints(id, useAmount);

        // 검증
        assertEquals(result.point(), prevAmount-useAmount);

        verify(pointHistoryTable, times(1))
                .insert(eq(id), eq(useAmount), eq(TransactionType.USE), anyLong());
    }
}
