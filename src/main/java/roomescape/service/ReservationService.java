package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationTime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.dto.reservation.AddReservationRequest;
import roomescape.dto.reservation.UpdateReservationRequest;
import roomescape.exception.dto.ErrorCode;
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
    public Reservation book(AddReservationRequest addReservationRequest) {
        LocalDate reservationDate = addReservationRequest.date();
        validateDate(reservationDate);

        Long timeId = addReservationRequest.timeId();
        ReservationTime reservationTime = reservationTimeRepository.getById(timeId)
                .orElseThrow(() -> new NotFoundResourceException(NOT_FOUND_RESERVATION_TIME));

        if (reservationDate.isEqual(LocalDate.now())) {
            reservationTime.validateTime();
        }

        Long themeId = addReservationRequest.themeId();
        Theme theme = themeRepository.getById(themeId)
                .orElseThrow(() -> new NotFoundResourceException(NOT_FOUND_THEME));

        if (reservationRepository.existsByTimeIdAndThemeIdAndDate(timeId, themeId, reservationDate)) {
            throw new DuplicatedResourceException(DUPLICATED_RESERVATION);
        }

        return reservationRepository.save(addReservationRequest.toReservation(reservationTime, theme));
    }

    @Transactional
    public void cancel(long id) {
        reservationRepository.deleteById(id);
    }

    @Transactional
    public void cancelByName(long id, String name) {
        Reservation reservation = reservationRepository.getById(id)
                .orElseThrow(() -> new NotFoundResourceException(NOT_FOUND_RESERVATION));

        reservation.validateOwner(name);

        reservationRepository.deleteById(id);
    }

    @Transactional
    public Reservation reschedule(long id, UpdateReservationRequest updateReservationRequest) {
        Reservation reservation = reservationRepository.getById(id)
                .orElseThrow(() -> new NotFoundResourceException(NOT_FOUND_RESERVATION));

        if (!reservation.name().equals(updateReservationRequest.name())) {
            throw new InvalidRequestException(UNAUTHORIZED_RESERVATION_ACCESS);
        }

        LocalDate updateDate = updateReservationRequest.date();
        validateDate(updateDate);

        Long updateTimeId = updateReservationRequest.timeId();
        ReservationTime reservationTime = reservationTimeRepository.getById(updateTimeId)
                .orElseThrow(() -> new NotFoundResourceException(NOT_FOUND_RESERVATION_TIME));

        if (updateDate.isEqual(LocalDate.now())) {
            reservationTime.validateTime();
        }

        Long themeId = reservation.theme().id();
        if (reservationRepository.existsByTimeIdAndThemeIdAndDate(updateTimeId, themeId, updateDate)) {
            throw new DuplicatedResourceException(DUPLICATED_RESERVATION);
        }

        return reservationRepository.updateDateAndTime(id, updateDate, updateTimeId);
    }

    private void validateDate(LocalDate reservationDate) {
        LocalDate today = LocalDate.now();

        if (reservationDate.isBefore(today)) {
            throw new InvalidRequestException(INVALID_RESERVATION_DATE);
        }
    }
}
