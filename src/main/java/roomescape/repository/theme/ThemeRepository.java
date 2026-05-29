package roomescape.repository.theme;

import roomescape.domain.theme.PopularThemeCondition;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeWithCount;
import roomescape.dto.theme.PopularThemeRequest;

import java.util.List;
import java.util.Optional;

public interface ThemeRepository {
    Theme addTheme(Theme theme);
    List<Theme> getAllTheme();
    Optional<Theme> getTheme(long id);
    void deleteTheme(long id);
    List<ThemeWithCount> getPopularTheme(PopularThemeCondition popularThemeCondition);
    boolean existsByName(String name);
}
