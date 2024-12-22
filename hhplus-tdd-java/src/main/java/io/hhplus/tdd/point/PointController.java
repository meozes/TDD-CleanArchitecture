package io.hhplus.tdd.point;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/point")
public class PointController {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);
    private final PointService pointService;

    /**
     * 포인트 조회
     */
    @GetMapping("{id}")
    public UserPoint point(
            @PathVariable long id
    ) {
        return pointService.getPointByUser(id);
    }

    /**
     * 포인트 히스토리(충전/이용 내역) 조회
     */
    @GetMapping("{id}/histories")
    public List<PointHistory> history(
            @PathVariable long id
    ) {
        return pointService.getPointHistoriesByUser(id);
    }

    /**
     * 포인트 충전
     */
    @PatchMapping("{id}/charge")
    public UserPoint charge(
            @PathVariable long id,
            @RequestBody long amount
    ) {
        return pointService.chargePoints(id, amount);
    }

    /**
     * 포인트 사용
     */
    @PatchMapping("{id}/use")
    public UserPoint use(
            @PathVariable long id,
            @RequestBody long amount
    ) {
        return pointService.usePoints(id, amount);
    }
}
