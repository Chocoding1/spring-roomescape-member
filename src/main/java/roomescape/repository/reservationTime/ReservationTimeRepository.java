package roomescape.repository.reservationTime;

import roomescape.domain.reservationTime.ReservationTime;
import roomescape.domain.reservationTime.ReservationTimeCondition;
import roomescape.domain.reservationTime.ReservationTimeWithAvailable;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationTimeRepository {
    ReservationTime save(ReservationTime reservationTime);
    Optional<ReservationTime> getById(long id);
    List<ReservationTime> getAll();
    void deleteById(long id);
    List<ReservationTimeWithAvailable> getAvailableReservationTimeByDateAndTheme(ReservationTimeCondition reservationTimeCondition);
    boolean existsByStartAt(LocalTime startAt);
}
