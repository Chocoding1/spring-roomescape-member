package roomescape.repository.reservation;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationTime.ReservationTime;
import roomescape.domain.theme.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcReservationRepository implements ReservationRepository {
    private static final String TABLE_NAME = "reservation";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TIME_ID = "time_id";
    private static final String COLUMN_THEME_ID = "theme_id";

    private static final String ALIAS_TIME_ID = "timeId";
    private static final String ALIAS_START_AT = "startAt";

    private static final String ALIAS_THEME_ID = "themeId";
    private static final String ALIAS_THEME_NAME = "themeName";
    private static final String ALIAS_THEME_DESCRIPTION = "themeDescription";
    private static final String ALIAS_THEME_IMAGE_URL = "themeImageUrl";

    private static final String SELECT_ALL_SQL = """
        SELECT
            r.id AS id,
            r.name AS name,
            r.date AS date,
            rt.id AS timeId,
            rt.start_at AS startAt,
            t.id AS themeId,
            t.name AS themeName,
            t.description AS themeDescription,
            t.image_url AS themeImageUrl
        FROM reservation AS r
        JOIN reservation_time AS rt ON r.time_id = rt.id
        JOIN theme AS t ON r.theme_id = t.id
    """;

    private static final RowMapper<Reservation> MAPPER = (rs, rowNumber) -> new Reservation(
            rs.getLong(COLUMN_ID),
            rs.getString(COLUMN_NAME),
            rs.getObject(COLUMN_DATE, LocalDate.class),
            new ReservationTime(
                    rs.getLong(ALIAS_TIME_ID),
                    rs.getObject(ALIAS_START_AT, LocalTime.class)
            ),
            new Theme(
                    rs.getLong(ALIAS_THEME_ID),
                    rs.getString(ALIAS_THEME_NAME),
                    rs.getString(ALIAS_THEME_DESCRIPTION),
                    rs.getString(ALIAS_THEME_IMAGE_URL)
            )
    );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(TABLE_NAME)
                .usingGeneratedKeyColumns(COLUMN_ID)
                .usingColumns("name", "date", "time_id", "theme_id");
    }

    @Override
    public List<Reservation> getAll() {
        return Collections.unmodifiableList(jdbcTemplate.query(SELECT_ALL_SQL, MAPPER));
    }

    @Override
    public List<Reservation> getAllByName(String name) {
        return Collections.unmodifiableList(jdbcTemplate.query(SELECT_ALL_SQL + "WHERE r.name = ?", MAPPER, name));
    }

    @Override
    public Reservation save(Reservation reservation) {
        long id = simpleJdbcInsert.executeAndReturnKey(Map.of(
                COLUMN_NAME, reservation.name(),
                COLUMN_DATE, reservation.date(),
                COLUMN_TIME_ID, reservation.time().id(),
                COLUMN_THEME_ID, reservation.theme().id()
        )).longValue();

        return new Reservation(id, reservation.name(), reservation.date(), reservation.time(),
                reservation.theme());
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM reservation WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsByTimeId(long timeId) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                    FROM reservation
                    WHERE time_id = ?
            )
    """;
        return jdbcTemplate.queryForObject(sql, Boolean.class, timeId) == Boolean.TRUE;
    }

    @Override
    public boolean existsByThemeId(long themeId) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                    FROM reservation
                    WHERE theme_id = ?
            )
    """;
        return jdbcTemplate.queryForObject(sql, Boolean.class, themeId) == Boolean.TRUE;
    }

    @Override
    public boolean existsByTimeIdAndThemeIdAndDate(long timeId, long themeId, LocalDate date) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                    FROM reservation
                    WHERE time_id = ?
                    AND theme_id = ?
                    AND date = ?
            )
            """;

        return jdbcTemplate.queryForObject(sql, Boolean.class, timeId, themeId, date) == Boolean.TRUE;
    }

    @Override
    public Optional<Reservation> getById(long id) {
        String sql = SELECT_ALL_SQL + "WHERE r.id = ?";

        List<Reservation> results = jdbcTemplate.query(sql, MAPPER, id);
        return results.stream().findFirst();
    }

    @Override
    public Reservation updateDateAndTime(long id, LocalDate date, long reservationTimeId) {
        String sql = """
        UPDATE reservation
        SET date = ?, time_id = ?
        WHERE id = ?
        """;

        jdbcTemplate.update(sql, date, reservationTimeId, id);
        return getById(id).get();
    }
}
