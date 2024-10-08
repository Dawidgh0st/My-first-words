package pl.kasprzak.dawid.myfirstwords.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.kasprzak.dawid.myfirstwords.model.words.CreateWordRequest;
import pl.kasprzak.dawid.myfirstwords.model.words.CreateWordResponse;
import pl.kasprzak.dawid.myfirstwords.model.words.GetAllWordsResponse;
import pl.kasprzak.dawid.myfirstwords.model.words.GetWordResponse;
import pl.kasprzak.dawid.myfirstwords.security.annotations.ChildOwnerOrAdmin;
import pl.kasprzak.dawid.myfirstwords.security.annotations.IsLoggedUser;
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
            @ApiResponse(responseCode = "403", description = "Access denied, authentication required"),
            @ApiResponse(responseCode = "404", description = "Parent or child not found")
    })
    @IsLoggedUser
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/{childId}")
    public CreateWordResponse addWord(@PathVariable Long childId,
                                      @Valid @RequestBody CreateWordRequest request) {
        return createWordService.addWord(childId, request);
    }

    @Operation(summary = "Delete a word by ID",
            description = "Deletes a word by its ID for the specified child for the authenticated parent or an administrator. " +
                    "If the authenticated user is a parent, they can delete a word for their own child without providing a parentID. " +
                    "If the authenticated user is an administrator, they must provide a parentID to delete a word associated with a child of that parent.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Word successfully deleted"),
            @ApiResponse(responseCode = "400", description = "Bad Request, parentID is required for administrators"),
            @ApiResponse(responseCode = "403", description = "Access denied, parent is not the owner of the child or user is not an administrator"),
            @ApiResponse(responseCode = "404", description = "Parent, child or word not found")
    })
    @ChildOwnerOrAdmin
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/{childId}/{wordId}")
    public void deleteWord(@PathVariable Long childId,
                           @PathVariable Long wordId,
                           @RequestParam(value = "parentID", required = false) Long parentID) {
        deleteWordService.deleteWord(childId, wordId, parentID);
    }

    @Operation(summary = "Get words before a specified date",
            description = "Fetches all words added before the specified date for the specified child. " +
                    "If the authenticated user is a parent, they can retrieve words for their own child without providing a parentID. " +
                    "If the authenticated user is an administrator, they must provide a parentID to retrieve words associated with a child of that parent.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Words successfully retrieved"),
            @ApiResponse(responseCode = "400", description = "Bad Request, parentID is required for administrators"),
            @ApiResponse(responseCode = "403", description = "Access denied, parent is not the owner of the child or user is not an administrator"),
            @ApiResponse(responseCode = "404", description = "Parent or child not found")
    })
    @ChildOwnerOrAdmin
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{childId}/before/{date}")
    public List<GetWordResponse> getByDateAchieveBefore(@PathVariable Long childId,
                                                        @PathVariable LocalDate date,
                                                        @RequestParam(value = "parentID", required = false) Long parentID) {
        return getWordService.getByDateAchieveBefore(childId, date, parentID);
    }

    @Operation(summary = "Get words after a specified date",
            description = "Fetches all words added after the specified date for the specified child. " +
                    "If the authenticated user is a parent, they can retrieve words for their own child without providing a parentID. " +
                    "If the authenticated user is an administrator, they must provide a parentID to retrieve words associated with a child of that parent.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Words successfully retrieved"),
            @ApiResponse(responseCode = "400", description = "Bad Request, parentID is required for administrators"),
            @ApiResponse(responseCode = "403", description = "Access denied, parent is not the owner of the child or user is not an administrator"),
            @ApiResponse(responseCode = "404", description = "Parent or child not found")
    })
    @ChildOwnerOrAdmin
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{childId}/after/{date}")
    public List<GetWordResponse> getByDateAchieveAfter(@PathVariable Long childId,
                                                       @PathVariable LocalDate date,
                                                       @RequestParam(value = "parentID", required = false) Long parentID) {
        return getWordService.getByDateAchieveAfter(childId, date, parentID);
    }

    @Operation(summary = "Get words between a specified dates",
            description = "Fetches all words added between the specified dates for the specified child. " +
                    "If the authenticated user is a parent, they can retrieve words for their own child without providing a parentID. " +
                    "If the authenticated user is an administrator, they must provide a parentID to retrieve words associated with a child of that parent.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Words successfully retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid date range"),
            @ApiResponse(responseCode = "400", description = "Bad Request, parentID is required for administrators"),
            @ApiResponse(responseCode = "403", description = "Access denied, parent is not the owner of the child or user is not an administrator"),
            @ApiResponse(responseCode = "404", description = "Parent or child not found")
    })
    @ChildOwnerOrAdmin
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{childId}/between")
    public List<GetWordResponse> getWordsBetweenDays(@PathVariable Long childId,
                                                     @RequestParam LocalDate startDate,
                                                     @RequestParam LocalDate endDate,
                                                     @RequestParam(value = "parentID", required = false) Long parentID) {
        return getWordService.getWordsBetweenDays(childId, startDate, endDate, parentID);
    }

    @Operation(summary = "Get all words for a child",
            description = "Fetches all words for a specific child. " +
                    "If the authenticated user is a parent, they can retrieve words for their own child without providing a parentID. " +
                    "If the authenticated user is an administrator, they must provide a parentID to retrieve words associated with a child of that parent.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Words successfully retrieved"),
            @ApiResponse(responseCode = "400", description = "Bad Request, parentID is required for administrators"),
            @ApiResponse(responseCode = "403", description = "Access denied, parent is not the owner of the child or user is not an administrator"),
            @ApiResponse(responseCode = "404", description = "Parent or child not found")
    })
    @ChildOwnerOrAdmin
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{childId}")
    public GetAllWordsResponse getAllWords(@PathVariable Long childId,
                                           @RequestParam(value = "parentID", required = false) Long parentID) {
        return getWordService.getAllWords(childId, parentID);
    }

    @Operation(summary = "Get word by exact match",
            description = "Fetches a specific word spoken by the specified child. " +
                    "If the authenticated user is a parent, they can retrieve a word for their own child without providing a parentID. " +
                    "If the authenticated user is an administrator, they must provide a parentID to retrieve a word associated with a child of that parent.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Word successfully retrieved"),
            @ApiResponse(responseCode = "400", description = "Bad Request, parentID is required for administrators"),
            @ApiResponse(responseCode = "403", description = "Access denied, parent is not the owner of the child or user is not an administrator"),
            @ApiResponse(responseCode = "404", description = "Parent, child or word not found")
    })
    @ChildOwnerOrAdmin
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "{childId}/word")
    public GetWordResponse getWordByChildIdAndWord(@PathVariable Long childId,
                                                   @RequestParam String word,
                                                   @RequestParam(value = "parentID", required = false) Long parentID) {
        return getWordService.getByWord(childId, word, parentID);
    }
}
