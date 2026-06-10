package roomescape.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservationTime.ReservationTime;
import roomescape.domain.reservationTime.ReservationTimeWithAvailable;
import roomescape.dto.reservationTime.AddReservationTimeRequest;
import roomescape.dto.reservationTime.AvailableReservationTimeRequest;
import roomescape.exception.exception.DataReferencedException;
import roomescape.exception.exception.DuplicatedResourceException;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservationTime.ReservationTimeRepository;

import java.util.List;

import static roomescape.exception.dto.ErrorCode.*;
import static roomescape.exception.dto.ErrorCode.DUPLICATED_RESERVATION_TIME;

@Service
public class ReservationTimeService {
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository, ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<ReservationTime> getAll() {
        return reservationTimeRepository.getAll();
    }

    @Transactional
    public ReservationTime register(AddReservationTimeRequest addReservationTimeRequest) {
        if (reservationTimeRepository.existsByStartAt(addReservationTimeRequest.startAt())) {
            throw new DuplicatedResourceException(DUPLICATED_RESERVATION_TIME);
        }

        return reservationTimeRepository.save(addReservationTimeRequest.toReservationTime());
    }

    @Transactional
    public void delete(long id) {
        boolean hasTimeId = reservationRepository.existsByTimeId(id);

        if(hasTimeId) {
            throw new DataReferencedException(CANNOT_DELETE_RESERVATION_TIME_IN_USE);
        }

        try {
            reservationTimeRepository.deleteById(id);
        }  catch(DataIntegrityViolationException e) {
            throw new DataReferencedException(INTEGRITY_VIOLATION_ON_DELETE);
        }
    }

    public List<ReservationTimeWithAvailable> getAvailableReservationTimeByDateAndTheme(
            AvailableReservationTimeRequest availableReservationTimeRequest
    ) {
        return reservationTimeRepository.getAvailableReservationTimeByDateAndTheme(
                availableReservationTimeRequest.toReservationTimeCondition()
        );
    }
}
