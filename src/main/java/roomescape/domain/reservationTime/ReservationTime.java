package roomescape.domain.reservationTime;

import roomescape.exception.exception.InvalidRequestException;

import java.time.LocalTime;

import static roomescape.exception.dto.ErrorCode.INVALID_RESERVATION_TIME;

public record ReservationTime(Long id, LocalTime startAt) {

    public ReservationTime(LocalTime startAt) {
        this(null, startAt);
    }

    public void validateTime() {
        if (startAt.isBefore(LocalTime.now())) {
            throw new InvalidRequestException(INVALID_RESERVATION_TIME);
        }
    }
}
