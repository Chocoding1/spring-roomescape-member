package roomescape.repository.theme;

import roomescape.domain.theme.PopularThemeCondition;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeWithCount;

import java.util.List;
import java.util.Optional;

public interface ThemeRepository {
    Theme save(Theme theme);
    List<Theme> getAll();
    Optional<Theme> getById(long id);
    void deleteById(long id);
    List<ThemeWithCount> getPopularTheme(PopularThemeCondition popularThemeCondition);
    boolean existsByName(String name);
}
