package roomescape.dto.reservationTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import roomescape.domain.reservationTime.ReservationTimeCondition;

import java.time.LocalDate;

public record AvailableReservationTimeRequest(
        @NotNull(message = "날짜는 필수입니다.")
        LocalDate date,

        @Min(value = 1, message = "themeId는 1 이상이어야 합니다.")
        long themeId
) {
    public ReservationTimeCondition toReservationTimeCondition() {
        return new ReservationTimeCondition(date, themeId);
    }
}
