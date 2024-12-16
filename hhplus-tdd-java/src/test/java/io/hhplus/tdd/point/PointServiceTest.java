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



import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;


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
        // 준비
        long userId = -1;
        long amount = 10000;

        // 실행 & 검증
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

        // 준비
        long userId = 1;
        long amount = -10000;

        // 실행 & 검증
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
        // 준비
        long userId = 1;
        long amount = 10000;

        UserPoint userPoint = new UserPoint(userId, amount, System.currentTimeMillis());

        // 유저가 존재하지 않는 경우 신규 등록
        given(userPointTable.selectById(anyLong())).willReturn(null); // 무조건 null 리턴하여 존재하지 않는 경우 만든다.
        given(userPointTable.insertOrUpdate(anyLong(), anyLong())).willReturn(userPoint);

        // 실행
        UserPoint result = pointService.chargePoints(userId, amount);

        // 검증
        assertThat(result.id()).isEqualTo(userPoint.id());
        assertThat(result.point()).isEqualTo(userPoint.point());
    }

    @Test
    public void 기존_회원_포인트_충전() {
        // 준비
        long userId = 2;
        long prevAmount = 20000;
        long chargeAmount = 30000;

        UserPoint userPoint = new UserPoint(userId, prevAmount, System.currentTimeMillis()); // 기존 회원
        given(userPointTable.selectById(userId)).willReturn(userPoint);
        given(userPointTable.insertOrUpdate(anyLong(), anyLong())).willReturn(new UserPoint(userId, prevAmount+chargeAmount, System.currentTimeMillis())); //추가 충전

        // 실행
        UserPoint result = pointService.chargePoints(userId, prevAmount+chargeAmount);

        // 검증
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.point()).isEqualTo(prevAmount+chargeAmount);

    }

    @Test
    public void 존재하지_않는_회원_조회() {
        // 준비
        long id = 100;

        // 실행
        given(userPointTable.selectById(id)).willReturn(null);

        //검증
        CustomException e = assertThrows(
                CustomException.class,
                () -> pointService.getPointByUser(id)
        );

        assertEquals(
                ErrorCode.USER_NOT_FOUND.getCode(), e.getErrorCode().getCode()
        );

    }

    
}
