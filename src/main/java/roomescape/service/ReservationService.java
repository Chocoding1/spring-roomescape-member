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

    public List<Reservation> getAllReservation() {
        return reservationRepository.getAllReservation();
    }

    public List<Reservation> getAllReservationsByName(String name) {
        return reservationRepository.getAllReservationByName(name);
    }

    @Transactional
    public Reservation addReservation(AddReservationRequest addReservationRequest) {
        LocalDate reservationDate = addReservationRequest.date();
        validateDate(reservationDate);

        ReservationTime reservationTime = reservationTimeRepository.getReservationTime(addReservationRequest.timeId())
                .orElseThrow(() -> new NotFoundResourceException(NOT_FOUND_RESERVATION_TIME));

        if (reservationDate.isEqual(LocalDate.now())) {
            reservationTime.validateTime();
        }

        Theme theme = themeRepository.getTheme(addReservationRequest.themeId())
                .orElseThrow(() -> new NotFoundResourceException(NOT_FOUND_THEME));

        if (reservationRepository.existsByTimeIdAndThemeIdAndDate(
                addReservationRequest.timeId(),
                addReservationRequest.themeId(),
                addReservationRequest.date())
        ) {
            throw new DuplicatedResourceException(DUPLICATED_RESERVATION);
        }

        return reservationRepository.addReservation(addReservationRequest.toReservation(reservationTime, theme));
    }

    @Transactional
    public void deleteReservation(long id) {
        reservationRepository.deleteReservation(id);
    }

    @Transactional
    public void deleteReservationByName(long id, String name) {
        Reservation reservation = reservationRepository.getReservationById(id)
                .orElseThrow(() -> new NotFoundResourceException(NOT_FOUND_RESERVATION));

        if (!reservation.name().equals(name)) {
            throw new InvalidRequestException(ErrorCode.UNAUTHORIZED_RESERVATION_ACCESS);
        }

        reservationRepository.deleteReservation(id);
    }

    @Transactional
    public Reservation updateReservation(long id, UpdateReservationRequest updateReservationRequest) {
        Reservation reservation = reservationRepository.getReservationById(id)
                .orElseThrow(() -> new NotFoundResourceException(NOT_FOUND_RESERVATION));

        if (!reservation.name().equals(updateReservationRequest.name())) {
            throw new InvalidRequestException(UNAUTHORIZED_RESERVATION_ACCESS);
        }

        LocalDate reservationDate = updateReservationRequest.date();
        validateDate(reservationDate);

        ReservationTime reservationTime = reservationTimeRepository.getReservationTime(updateReservationRequest.timeId())
                .orElseThrow(() -> new NotFoundResourceException(NOT_FOUND_RESERVATION_TIME));

        if (reservationDate.isEqual(LocalDate.now())) {
            reservationTime.validateTime();
        }

        if (reservationRepository.existsByTimeIdAndThemeIdAndDate(
                updateReservationRequest.timeId(),
                reservation.theme().id(),
                updateReservationRequest.date()
        )) {
            throw new DuplicatedResourceException(DUPLICATED_RESERVATION);
        }

        return reservationRepository.updateReservation(id, updateReservationRequest.date(), reservationTime.id());
    }

    private void validateDate(LocalDate reservationDate) {
        LocalDate today = LocalDate.now();

        if (reservationDate.isBefore(today)) {
            throw new InvalidRequestException(INVALID_RESERVATION_DATE);
        }
    }
}
