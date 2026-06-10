package roomescape.repository.reservation;

import roomescape.domain.reservation.Reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
    List<Reservation> getAll();
    List<Reservation> getAllByName(String name);
    Reservation save(Reservation reservation);
    void deleteById(long id);
    boolean existsByTimeId(long timeId);
    boolean existsByThemeId(long themeId);
    boolean existsByTimeIdAndThemeIdAndDate(long timeId, long themeId, LocalDate date);
    Optional<Reservation> getById(long id);
    Reservation updateDateAndTime(long id, LocalDate date, long reservationTimeId);
}
