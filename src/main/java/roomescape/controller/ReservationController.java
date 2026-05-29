package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.domain.reservation.Reservation;
import roomescape.dto.reservation.*;
import roomescape.service.ReservationService;

import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping()
    public ResponseEntity<List<ReservationResponse>> getReservations() {
        List<Reservation> reservations = reservationService.getAllReservation();
        List<ReservationResponse> reservationResponses = reservations.stream()
                .map(ReservationResponse::from)
                .toList();

        return ResponseEntity.ok(reservationResponses);
    }

    @PostMapping()
    public ResponseEntity<ReservationResponse> addReservation(
            @RequestBody @Valid AddReservationRequest addReservationRequest
    ) {
        Reservation addedReservation = reservationService.addReservation(addReservationRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(ReservationResponse.from(addedReservation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") long id) {
        reservationService.deleteReservation(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping(params = {"name"})
    public ResponseEntity<List<ReservationResponse>> getReservationsByName(
            @ModelAttribute @Valid GetReservationByNameRequest getReservationByNameRequest
    ) {
        List<Reservation> reservations = reservationService.getAllReservationsByName(getReservationByNameRequest);
        List<ReservationResponse> reservationResponses = reservations.stream()
                .map(ReservationResponse::from)
                .toList();

        return ResponseEntity.ok(reservationResponses);
    }

    @DeleteMapping(value = "/{id}", params = {"name"})
    public ResponseEntity<Void> deleteReservationByName(
            @PathVariable("id") long id,
            @ModelAttribute @Valid DeleteReservationRequest deleteReservationRequest
    ) {
        reservationService.deleteReservationByName(id, deleteReservationRequest.name());

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable long id,
            @RequestBody @Valid UpdateReservationRequest updateReservationRequest
    ) {
        Reservation reservation = reservationService.updateReservation(id, updateReservationRequest);

        return ResponseEntity.ok(ReservationResponse.from(reservation));
    }
}
