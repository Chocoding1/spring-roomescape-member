package roomescape.domain.theme;

public record ThemeWithCount(long id, String name, String description, String imageUrl, long count) {
}
