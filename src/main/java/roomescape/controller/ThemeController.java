package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeWithCount;
import roomescape.dto.theme.AddThemeRequest;
import roomescape.dto.theme.PopularThemeRequest;
import roomescape.dto.theme.PopularThemeResponse;
import roomescape.dto.theme.ThemeResponse;
import roomescape.service.ThemeService;

import java.util.List;

@RestController
@RequestMapping("/themes")
public class ThemeController {
    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping()
    public ResponseEntity<List<ThemeResponse>> getThemes() {
        List<Theme> themes = themeService.getAll();
        List<ThemeResponse> themeResponses = themes.stream()
                .map(ThemeResponse::from)
                .toList();

        return ResponseEntity.ok(themeResponses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ThemeResponse> getTheme(@PathVariable long id) {
        Theme theme = themeService.getById(id);

        return ResponseEntity.ok(ThemeResponse.from(theme));
    }

    @PostMapping()
    public ResponseEntity<ThemeResponse> createTheme(@RequestBody @Valid AddThemeRequest addThemeRequest) {
        Theme addedTheme = themeService.register(addThemeRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(ThemeResponse.from(addedTheme));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable long id) {
        themeService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/popular", params = {"startDate", "endDate", "size"})
    public ResponseEntity<List<PopularThemeResponse>> getPopularTheme(
            @ModelAttribute @Valid PopularThemeRequest popularThemeRequest
    ) {
        List<ThemeWithCount> themeWithCounts = themeService.getPopularTheme(popularThemeRequest);
        List<PopularThemeResponse> popularThemeResponses = themeWithCounts.stream()
                .map(PopularThemeResponse::from)
                .toList();

        return ResponseEntity.ok(popularThemeResponses);
    }
}
