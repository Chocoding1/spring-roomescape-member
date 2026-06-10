package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.domain.reservationTime.ReservationTime;
import roomescape.domain.reservationTime.ReservationTimeWithAvailable;
import roomescape.dto.reservationTime.AddReservationTimeRequest;
import roomescape.dto.reservationTime.AvailableReservationTimeRequest;
import roomescape.dto.reservationTime.AvailableReservationTimeResponse;
import roomescape.dto.reservationTime.ReservationTimeResponse;
import roomescape.service.ReservationTimeService;

import java.util.List;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {
    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping()
    public ResponseEntity<List<ReservationTimeResponse>> getReservationTimes() {
        List<ReservationTime> reservationTimes = reservationTimeService.getAll();
        List<ReservationTimeResponse> reservationTimeResponses = reservationTimes.stream()
                .map(ReservationTimeResponse::from)
                .toList();

        return ResponseEntity.ok(reservationTimeResponses);
    }

    @PostMapping()
    public ResponseEntity<ReservationTimeResponse> createReservationTime(
            @RequestBody @Valid AddReservationTimeRequest addReservationTimeRequest
    ) {
        ReservationTime reservationTime = reservationTimeService.register(addReservationTimeRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(ReservationTimeResponse.from(reservationTime));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationTime(@PathVariable("id") long id) {
        reservationTimeService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/availability", params = {"date", "themeId"})
    public ResponseEntity<List<AvailableReservationTimeResponse>> getAvailableReservationTimeByDateAndTheme(
            @ModelAttribute @Valid AvailableReservationTimeRequest availableReservationTimeRequest
    ) {
        List<ReservationTimeWithAvailable> reservationTimesWithAvailable  = reservationTimeService
                .getAvailableReservationTimeByDateAndTheme(availableReservationTimeRequest);

        List<AvailableReservationTimeResponse> availableReservationTimeResponses = reservationTimesWithAvailable.stream()
                .map(AvailableReservationTimeResponse::from)
                .toList();

        return ResponseEntity.ok(availableReservationTimeResponses);
    }
}
