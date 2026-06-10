package roomescape.domain.reservation;

import roomescape.domain.theme.Theme;
import roomescape.domain.reservationTime.ReservationTime;
import roomescape.exception.dto.ErrorCode;
import roomescape.exception.exception.InvalidRequestException;

import java.time.LocalDate;

public record Reservation(Long id, String name, LocalDate date, ReservationTime time, Theme theme) {

    public Reservation(String name, LocalDate date, ReservationTime time, Theme theme) {
        this(null, name, date, time, theme);
    }

    public void validateOwner(String name) {
        if (!this.name().equals(name)) {
            throw new InvalidRequestException(ErrorCode.UNAUTHORIZED_RESERVATION_ACCESS);
        }
    }
}
