package io.hhplus.tdd.point;


import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.mockito.InjectMocks;
import org.mockito.Mock;

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


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pointService = new PointService(userPointTable);
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

        // then()
        assertThat(result.id()).isEqualTo(userPoint.id());
        assertThat(result.point()).isEqualTo(userPoint.point());

    }

    @Test
    public void 음수_포인트() {

        // given
        long userId = 1L;
        long amount = -10000;

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> pointService.chargePoints(userId, amount)
        );

        assertEquals(
                "java.lang.IllegalArgumentException: 포인트는 음수 값이 될 수 없습니다.",
                exception.toString()
        );

    }
}
