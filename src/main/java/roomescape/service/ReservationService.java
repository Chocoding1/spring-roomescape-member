package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationTime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.dto.reservation.AddReservationRequest;
import roomescape.dto.reservation.UpdateReservationRequest;
import roomescape.exception.exception.DuplicatedResourceException;
import roomescape.exception.exception.InvalidRequestException;
import roomescape.exception.exception.NotFoundResourceException;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservationTime.ReservationTimeRepository;
import roomescape.repository.theme.ThemeRepository;

import java.time.LocalDate;
import java.util.List;

import static roomescape.exception.dto.ErrorCode.*;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(ReservationRepository reservationRepository, ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public List<Reservation> findAll() {
        return reservationRepository.getAll();
    }

    public List<Reservation> findAllByName(String name) {
        return reservationRepository.getAllByName(name);
    }

    @Transactional
    public Reservation book(AddReservationRequest request) {
        ReservationTime reservationTime = findReservationTime(request.timeId());
        Theme theme = findTheme(request.themeId());

        validateDuplication(request.timeId(), request.themeId(), request.date());

        Reservation reservation = request.toReservation(reservationTime, theme);
        reservation.validateBookingPolicy();

        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation reschedule(long id, UpdateReservationRequest request) {
        Reservation reservation = findReservation(id);
        reservation.validateOwner(request.name());

        validateSchedule(request.date(), request.timeId());
        validateDuplication(request.timeId(), reservation.theme().id(), request.date());

        return reservationRepository.updateDateAndTime(id, request.date(), request.timeId());
    }

    @Transactional
    public void cancel(long id) {
        reservationRepository.deleteById(id);
    }

    @Transactional
    public void cancelByName(long id, String name) {
        Reservation reservation = findReservation(id);

        reservation.validateOwner(name);

        reservationRepository.deleteById(id);
    }

    private ReservationTime findReservationTime(Long timeId) {
        return reservationTimeRepository.getById(timeId)
                .orElseThrow(() -> new NotFoundResourceException(NOT_FOUND_RESERVATION_TIME));
    }

    private Theme findTheme(Long themeId) {
        return themeRepository.getById(themeId)
                .orElseThrow(() -> new NotFoundResourceException(NOT_FOUND_THEME));
    }

    private void validateDuplication(Long timeId, Long themeId, LocalDate reservationDate) {
        if (reservationRepository.existsByTimeIdAndThemeIdAndDate(timeId, themeId, reservationDate)) {
            throw new DuplicatedResourceException(DUPLICATED_RESERVATION);
        }
    }

    private void validateSchedule(LocalDate date, Long timeId) {
        ReservationTime reservationTime = findReservationTime(timeId);

        validateDate(date);

        if (date.isEqual(LocalDate.now())) {
            reservationTime.validateTime();
        }
    }

    private Reservation findReservation(long id) {
        return reservationRepository.getById(id)
                .orElseThrow(() -> new NotFoundResourceException(NOT_FOUND_RESERVATION));
    }

    private void validateDate(LocalDate reservationDate) {
        if (reservationDate.isBefore(LocalDate.now())) {
            throw new InvalidRequestException(INVALID_RESERVATION_DATE);
        }
    }
}
