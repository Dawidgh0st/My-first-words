package pl.kasprzak.dawid.myfirstwords.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.kasprzak.dawid.myfirstwords.model.words.CreateWordRequest;
import pl.kasprzak.dawid.myfirstwords.model.words.CreateWordResponse;
import pl.kasprzak.dawid.myfirstwords.model.words.GetAllWordsResponse;
import pl.kasprzak.dawid.myfirstwords.model.words.GetWordResponse;
import pl.kasprzak.dawid.myfirstwords.service.words.CreateWordService;
import pl.kasprzak.dawid.myfirstwords.service.words.DeleteWordService;
import pl.kasprzak.dawid.myfirstwords.service.words.GetWordService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/words")
public class WordsController {

    private final CreateWordService createWordService;
    private final DeleteWordService deleteWordService;
    private final GetWordService getWordService;

    @Operation(summary = "Add a new word", description = "Creates a new word for the specified child. This endpoint is accessible to authenticated parents and verifies the parent-child relationship.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Word successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Parent or child not found")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/{childId}")
    public CreateWordResponse addWord(@PathVariable Long childId, @Valid @RequestBody CreateWordRequest request,
                                      @Parameter(hidden = true) Authentication authentication) {
        return createWordService.addWord(childId, request, authentication);
    }

    @Operation(summary = "Delete a word", description = "Deletes a word by its ID for the specified child. This endpoint is accessible to authenticated parents and verifies the parent-child relationship.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Word successfully deleted"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Parent, child or word not found")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/{childId}/{wordId}")
    public void deleteWord(@PathVariable Long childId, @PathVariable Long wordId,
                           @Parameter(hidden = true) Authentication authentication) {
        deleteWordService.deleteWord(childId, wordId, authentication);
    }

    @Operation(summary = "Get words before a date", description = "Fetches all words added before the specified date for a specific child. This endpoint is accessible to authenticated parents and verifies the parent-child relationship.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Words successfully retrieved"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Parent or child not found")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{childId}/before/{date}")
    public List<GetWordResponse> getByDateAchieveBefore(@PathVariable Long childId, @PathVariable LocalDate date,
                                                        @Parameter(hidden = true) Authentication authentication) {
        return getWordService.getByDateAchieveBefore(childId, date, authentication);
    }

    @Operation(summary = "Get words after a date", description = "Fetches all words added after the specified date for a specific child. This endpoint is accessible to authenticated parents and verifies the parent-child relationship.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Words successfully retrieved"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Parent or child not found")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{childId}/after/{date}")
    public List<GetWordResponse> getByDateAchieveAfter(@PathVariable Long childId, @PathVariable LocalDate date,
                                                       @Parameter(hidden = true) Authentication authentication) {
        return getWordService.getByDateAchieveAfter(childId, date, authentication);
    }

    @Operation(summary = "Get words between dates", description = "Fetches all words added between the specified start and end dates for a specific child. This endpoint is accessible to authenticated parents and verifies the parent-child relationship.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Words successfully retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid date range"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Parent or child not found")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{childId}/between")
    public List<GetWordResponse> getWordsBetweenDays(@PathVariable Long childId, @RequestParam LocalDate startDate,
                                                     @RequestParam LocalDate endDate,
                                                     @Parameter(hidden = true) Authentication authentication) {
        return getWordService.getWordsBetweenDays(childId, startDate, endDate, authentication);
    }

    @Operation(summary = "Get all words", description = "Fetches all words for a specific child. This endpoint is accessible to authenticated parents and verifies the parent-child relationship.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Words successfully retrieved"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Parent or child not found")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{childId}")
    public GetAllWordsResponse getAllWords(@PathVariable Long childId,
                                           @Parameter(hidden = true) Authentication authentication) {
        return getWordService.getAllWords(childId, authentication);
    }

    @Operation(summary = "Get word by title", description = "Fetches a word for a specific child based on the given word title. This endpoint is accessible to authenticated parents and verifies the parent-child relationship.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Word successfully retrieved"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Parent, child or word not found")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "{childId}/word")
    public GetWordResponse getWordByChildIdAndWord(@PathVariable Long childId, @RequestParam String word,
                                                   @Parameter(hidden = true) Authentication authentication) {
        return getWordService.getByWord(childId, word, authentication);
    }
}
