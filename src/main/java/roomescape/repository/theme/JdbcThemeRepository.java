package roomescape.repository.theme;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.theme.PopularThemeCondition;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeWithCount;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcThemeRepository implements ThemeRepository {
    private static final String TABLE_NAME = "theme";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_IMAGE_URL = "image_url";
    private static final String COLUMN_COUNT = "count";

    private static final RowMapper<Theme> MAPPER = (rs, rowNumber) -> new Theme(
            rs.getLong(COLUMN_ID),
            rs.getString(COLUMN_NAME),
            rs.getString(COLUMN_DESCRIPTION),
            rs.getString(COLUMN_IMAGE_URL)
    );

    private static final RowMapper<ThemeWithCount> THEME_WITH_COUNT_MAPPER = (rs, rowNumber) -> new ThemeWithCount(
            rs.getLong(COLUMN_ID),
            rs.getString(COLUMN_NAME),
            rs.getString(COLUMN_DESCRIPTION),
            rs.getString(COLUMN_IMAGE_URL),
            rs.getLong(COLUMN_COUNT)
    );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(TABLE_NAME)
                .usingGeneratedKeyColumns(COLUMN_ID);    }

    public Theme save(Theme theme) {
        long id = simpleJdbcInsert.executeAndReturnKey(Map.of(
                COLUMN_NAME, theme.name(),
                COLUMN_DESCRIPTION, theme.description(),
                COLUMN_IMAGE_URL, theme.imageUrl()
        )).longValue();

        return new Theme(id, theme.name(), theme.description(), theme.imageUrl());
    }

    public List<Theme> getAll() {
        String sql = "SELECT id, name, description, image_url FROM theme";

        return jdbcTemplate.query(sql, MAPPER);
    }

    public Optional<Theme> getById(long id) {
        String sql = "SELECT id, name, description, image_url FROM theme WHERE id = ?";

        return jdbcTemplate.query(sql, MAPPER, id)
                .stream()
                .findFirst();
    }

    public void deleteById(long id) {
        String sql = "DELETE FROM theme WHERE id = ?";

        jdbcTemplate.update(sql, id);
    }

    @Override
    public List<ThemeWithCount> getPopularTheme(PopularThemeCondition popularThemeCondition) {
        String sql = """
                    SELECT t.id AS id, t.name AS name, t.description AS description, t.image_url AS image_url, reservation_count AS count
                    FROM theme t
                    JOIN (
                        SELECT theme_id, COUNT(id) AS reservation_count
                        FROM reservation
                        WHERE created_at BETWEEN ? AND ?
                        GROUP BY theme_id
                    ) AS r ON r.theme_id = t.id
                    ORDER BY r.reservation_count DESC
                    LIMIT ?
                """;

        return jdbcTemplate.query(sql, THEME_WITH_COUNT_MAPPER,
                popularThemeCondition.startDate(),
                popularThemeCondition.endDate(),
                popularThemeCondition.size()
        );
    }

    @Override
    public boolean existsByName(String name) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM theme
                    WHERE name = ?
                )
                """;

        return jdbcTemplate.queryForObject(sql, Boolean.class, name) == Boolean.TRUE;
    }
}
