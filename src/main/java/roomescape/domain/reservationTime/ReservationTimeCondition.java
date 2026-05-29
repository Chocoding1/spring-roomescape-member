package roomescape.domain.reservationTime;

import java.time.LocalDate;

public record ReservationTimeCondition(LocalDate date, long themeId) {
}
