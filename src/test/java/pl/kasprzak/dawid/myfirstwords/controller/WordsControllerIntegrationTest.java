package pl.kasprzak.dawid.myfirstwords.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.kasprzak.dawid.myfirstwords.model.words.CreateWordRequest;
import pl.kasprzak.dawid.myfirstwords.model.words.CreateWordResponse;
import pl.kasprzak.dawid.myfirstwords.model.words.GetAllWordsResponse;
import pl.kasprzak.dawid.myfirstwords.model.words.GetWordResponse;
import pl.kasprzak.dawid.myfirstwords.repository.ChildrenRepository;
import pl.kasprzak.dawid.myfirstwords.repository.ParentsRepository;
import pl.kasprzak.dawid.myfirstwords.repository.WordsRepository;
import pl.kasprzak.dawid.myfirstwords.repository.dao.ChildEntity;
import pl.kasprzak.dawid.myfirstwords.repository.dao.ParentEntity;
import pl.kasprzak.dawid.myfirstwords.repository.dao.WordEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class WordsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WordsRepository wordsRepository;
    @Autowired
    private ParentsRepository parentsRepository;
    @Autowired
    private ChildrenRepository childrenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private ChildEntity childEntity;
    private CreateWordRequest createWordRequest;
    private List<GetWordResponse> allWordResponses;
    private WordEntity wordEntity1;
    private LocalDate date;

    @BeforeEach
    void setUp() {

        ParentEntity parentEntity = new ParentEntity();
        parentEntity.setUsername("user");
        parentEntity.setPassword(passwordEncoder.encode("password"));
        parentEntity = parentsRepository.save(parentEntity);

        childEntity = new ChildEntity();
        childEntity.setName("childName");
        childEntity.setParent(parentEntity);
        childEntity = childrenRepository.save(childEntity);

        createWordRequest = CreateWordRequest.builder()
                .word("word1")
                .dateAchieve(LocalDate.of(2024, 1, 1))
                .build();

        date = LocalDate.of(2024, 1, 1);

        wordEntity1 = new WordEntity();
        wordEntity1.setId(1L);
        wordEntity1.setWord(createWordRequest.getWord());
        wordEntity1.setDateAchieve(createWordRequest.getDateAchieve().minusDays(1));
        wordEntity1.setChild(childEntity);

        WordEntity wordEntity2 = new WordEntity();
        wordEntity2.setId(2L);
        wordEntity2.setWord("word2");
        wordEntity2.setDateAchieve(date.minusDays(2));
        wordEntity2.setChild(childEntity);

        WordEntity wordEntity3 = new WordEntity();
        wordEntity3.setId(3L);
        wordEntity3.setWord("word3");
        wordEntity3.setDateAchieve(date.plusDays(1));
        wordEntity3.setChild(childEntity);

        WordEntity wordEntity4 = new WordEntity();
        wordEntity4.setId(4L);
        wordEntity4.setWord("word4");
        wordEntity4.setDateAchieve(date.plusDays(2));
        wordEntity4.setChild(childEntity);

        List<WordEntity> wordEntities = Arrays.asList(wordEntity1, wordEntity2, wordEntity3, wordEntity4);

        wordsRepository.saveAll(wordEntities);

        allWordResponses = wordEntities.stream()
                .map(wordEntity -> GetWordResponse.builder()
                        .id(wordEntity.getId())
                        .word(wordEntity.getWord())
                        .dateAchieve(wordEntity.getDateAchieve())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Integration test for adding a word to a specific child's account.
     * This test verifies that a new word is successfully added to the specified child's vocabulary.
     * It checks that the response status is HTTP 201 Created, the response content matches the expected data,
     * and that the word is correctly stored in the database.
     *
     * @throws Exception if an error occurs during the request or response processing.
     */
    @Test
    @Transactional
    @WithUserDetails(userDetailsServiceBeanName = "userDetailsServiceForTest")
    void when_addWord_then_wordShouldBeAddedToSpecificChild() throws Exception {

        String jsonResponse = mockMvc.perform(post("/api/words/{childId}", childEntity.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createWordRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();


        CreateWordResponse response = objectMapper.readValue(jsonResponse, CreateWordResponse.class);

        assertEquals(createWordRequest.getWord(), response.getWord());
        assertEquals(createWordRequest.getDateAchieve(), response.getDateAchieve());

        List<WordEntity> words = wordsRepository.findAll();
        assertEquals(5, words.size());
        WordEntity wordEntity = words.get(0);
        assertEquals("word1", wordEntity.getWord());
        assertEquals(LocalDate.of(2023, 12, 31), wordEntity.getDateAchieve());
        assertEquals(childEntity.getId(), wordEntity.getChild().getId());
    }

    /**
     * Integration test for deleting a word from a child's account.
     * This test verifies that a specific word is successfully deleted from the child's vocabulary.
     * It checks that the response status is HTTP 204 No Content and that the word is no longer present in the database.
     *
     * @throws Exception if an error occurs during the request or response processing.
     */
    @Test
    @Transactional
    @WithUserDetails(userDetailsServiceBeanName = "userDetailsServiceForTest")
    void when_deleteWord_then_wordShouldBeDeletedFromChildAccount() throws Exception {
        mockMvc.perform(delete("/api/words/{childId}/{wordId}", childEntity.getId(), wordEntity1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertFalse(wordsRepository.findByChildIdAndId(childEntity.getId(), wordEntity1.getId()).isPresent());
    }

    /**
     * Integration test for attempting to delete a word that does not exist.
     * This test verifies that the service returns an HTTP 404 Not Found status with the appropriate error message
     * when trying to delete a non-existent word from the child's vocabulary.
     *
     * @throws Exception if an error occurs during the request or response processing.
     */
    @Test
    @Transactional
    @WithUserDetails(userDetailsServiceBeanName = "userDetailsServiceForTest")
    void when_deleteWordAndWordNotFound_then_throwWordNotFoundException() throws Exception {
        Long nonExistentWordId = 999L;

        mockMvc.perform(delete("/api/words/{childId}/{wordId}", childEntity.getId(), nonExistentWordId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Word not found"));
    }

    /**
     * Integration test for retrieving words learned before a given date.
     * This test verifies that the service returns a list of words that were added before the specified date.
     * It checks that the response status is HTTP 200 OK and that the returned JSON matches the expected data.
     *
     * @throws Exception if an error occurs during the request or response processing.
     */
    @Test
    @WithUserDetails(userDetailsServiceBeanName = "userDetailsServiceForTest")
    void when_getByDateAchieveBefore_then_wordsShouldBeReturnedBeforeTheGivenDate() throws Exception {

        List<GetWordResponse> expectedResponse = allWordResponses.stream()
                .filter(getWordResponse -> getWordResponse.getDateAchieve().isBefore(date))
                .collect(Collectors.toList());

        mockMvc.perform(get("/api/words/{childId}/before/{date}", childEntity.getId(), date)
                        .param("date", date.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));
    }

    /**
     * Integration test for retrieving words learned after a given date.
     * This test verifies that the service returns a list of words that were added after the specified date.
     * It checks that the response status is HTTP 200 OK and that the returned JSON matches the expected data.
     *
     * @throws Exception if an error occurs during the request or response processing.
     */
    @Test
    @WithUserDetails(userDetailsServiceBeanName = "userDetailsServiceForTest")
    void when_getByDateAchieveAfter_then_wordsShouldBeReturnedAfterTheGivenDate() throws Exception {

        List<GetWordResponse> expectedResponse = allWordResponses.stream()
                .filter(getWordResponse -> getWordResponse.getDateAchieve().isAfter(date))
                .collect(Collectors.toList());


        mockMvc.perform(get("/api/words/{childId}/after/{date}", childEntity.getId(), date)
                        .param("date", date.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));
    }

    /**
     * Integration test for retrieving words learned between two dates.
     * This test verifies that the service returns a list of words that were added between the specified
     * start and end dates.
     * It checks that the response status is HTTP 200 OK and that the returned JSON matches the expected data.
     *
     * @throws Exception if an error occurs during the request or response processing.
     */
    @Test
    @WithUserDetails(userDetailsServiceBeanName = "userDetailsServiceForTest")
    void when_getWordsBetweenDays_then_wordsShouldBeReturnedBetweenTheGivenDates() throws Exception {
        LocalDate startDate = date.minusDays(2);
        LocalDate endDate = date.plusDays(2);

        List<GetWordResponse> expectedResponse = allWordResponses.stream()
                .filter(getWordResponse -> !getWordResponse.getDateAchieve().isBefore(startDate) && !getWordResponse.getDateAchieve().isAfter(endDate))
                .collect(Collectors.toList());

        mockMvc.perform(get("/api/words/{childId}/between", childEntity.getId())
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));
    }

    //sprawdzić komunikat

    /**
     * Integration test for retrieving words with a null start date.
     * This test verifies that the service returns an HTTP 400 Bad Request status
     * when the start date is null, as the date range is invalid.
     *
     * @throws Exception if an error occurs during the request or response processing.
     */
    @Test
    @WithUserDetails(userDetailsServiceBeanName = "userDetailsServiceForTest")
    void when_getWordsBetweenDays__and_startDateIsNull_then_throwDateValidationException() throws Exception {
        LocalDate endDate = date.plusDays(2);

        mockMvc.perform(get("/api/words/{childId}/between", childEntity.getId())
                        .param("endDate", endDate.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    //sprawdzić komunikat

    /**
     * Integration test for retrieving words with a null end date.
     * This test verifies that the service returns an HTTP 400 Bad Request status
     * when the end date is null, as the date range is invalid.
     *
     * @throws Exception if an error occurs during the request or response processing.
     */
    @Test
    @WithUserDetails(userDetailsServiceBeanName = "userDetailsServiceForTest")
    void when_getWordsBetweenDays_and_endDateIsNull_then_throwDateValidationException() throws Exception {
        LocalDate startDate = date.minusDays(2);

        mockMvc.perform(get("/api/words/{childId}/between", childEntity.getId())
                        .param("startDate", startDate.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    /**
     * Integration test for retrieving words when the start date is after the end date.
     * This test verifies that the service returns an HTTP 400 Bad Request status with the appropriate error message
     * when the start date is after the end date.
     *
     * @throws Exception if an error occurs during the request or response processing.
     */
    @Test
    @WithUserDetails(userDetailsServiceBeanName = "userDetailsServiceForTest")
    void when_getWordsBetweenDays_and_startDateIsAfterEndDate_then_throwInvalidDateOrderException() throws Exception {
        LocalDate startDate = date.plusDays(2);
        LocalDate endDate = date.minusDays(2);

        mockMvc.perform(get("/api/words/{childId}/between", childEntity.getId())
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Start date must be before or equal to end date"));
    }

    /**
     * Integration test for retrieving all words for a specific child.
     * This test verifies that the service returns a complete list of all words associated with the child.
     * It checks that the response status is HTTP 200 OK and that the returned JSON matches the expected data.
     *
     * @throws Exception if an error occurs during the request or response processing.
     */
    @Test
    @WithUserDetails(userDetailsServiceBeanName = "userDetailsServiceForTest")
    void when_getAllWords_then_allWordsTheChildShouldBeReturned() throws Exception {

        GetAllWordsResponse expectedResponse = GetAllWordsResponse.builder()
                .words(allWordResponses)
                .build();

        mockMvc.perform(get("/api/words/{childId}", childEntity.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));
    }

    /**
     * Integration test for retrieving a specific word for a child by the word content.
     * This test verifies that the service correctly retrieves a word associated with the given child ID and word.
     * It checks that the response status is HTTP 200 OK and that the returned JSON contains the expected word details.
     *
     * @throws Exception if an error occurs during the request or response processing.
     */
    @Test
    @WithUserDetails(userDetailsServiceBeanName = "userDetailsServiceForTest")
    void when_getWordByChildIdAndWord_then_wordShouldBeReturn() throws Exception {
        String word = "word1";

        mockMvc.perform(get("/api/words/{childId}/word", childEntity.getId())
                        .param("word", word.toLowerCase())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.word").value(word));
    }

    /**
     * Integration test for retrieving a word for a child when the word does not exist.
     * This test verifies that the service returns an HTTP 404 Not Found status with the appropriate error message
     * when the specified word is not found for the given child ID.
     *
     * @throws Exception if an error occurs during the request or response processing.
     */
    @Test
    @WithUserDetails(userDetailsServiceBeanName = "userDetailsServiceForTest")
    void when_getWordByChildIdAndWord_then_throwWordNotFoundException() throws Exception {
        String word = "nonexistentWord";

        mockMvc.perform(get("/api/words/{childId}/word", childEntity.getId())
                        .param("word", word.toLowerCase())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Word not found"));

    }
}


