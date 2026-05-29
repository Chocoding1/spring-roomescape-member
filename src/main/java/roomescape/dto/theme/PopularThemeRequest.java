package roomescape.dto.theme;

import jakarta.validation.constraints.NotNull;
import roomescape.domain.theme.PopularThemeCondition;

import java.time.LocalDate;

public record PopularThemeRequest(

        @NotNull(message = "시작 날짜는 반드시 포함되어야 합니다.")
        LocalDate startDate,

        @NotNull(message = "종료 날짜는 반드시 포함되어야 합니다.")
        LocalDate endDate,

        @NotNull(message = "조회 개수는 반드시 포함되어야 합니다.")
        Long size
) {
    public PopularThemeCondition toPopularThemeCondition() {
        return new PopularThemeCondition(startDate, endDate, size);
    }
}
