package pl.kasprzak.dawid.myfirstwords.service.words;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import pl.kasprzak.dawid.myfirstwords.service.DateRangeGeneralService;
import pl.kasprzak.dawid.myfirstwords.util.AuthorizationHelper;
import pl.kasprzak.dawid.myfirstwords.model.words.GetAllWordsResponse;
import pl.kasprzak.dawid.myfirstwords.model.words.GetWordResponse;
import pl.kasprzak.dawid.myfirstwords.repository.WordsRepository;
import pl.kasprzak.dawid.myfirstwords.repository.dao.WordEntity;
import pl.kasprzak.dawid.myfirstwords.service.converters.words.GetWordsConverter;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetWordService {

    private final WordsRepository wordsRepository;
    private final GetWordsConverter getWordsConverter;
    private final AuthorizationHelper authorizationHelper;
    private final DateRangeGeneralService dateRangeGeneralService;

    public List<GetWordResponse> getByDateAchieveBefore(Long childId, LocalDate date, Authentication authentication) {
        List<WordEntity> words = dateRangeGeneralService.getByDateAchieveBefore(childId, date, authentication,
                wordsRepository::findByChildIdAndDateAchieveBefore);
        return words.stream()
                .map(getWordsConverter::toDto)
                .collect(Collectors.toList());
    }

    public List<GetWordResponse> getByDateAchieveAfter(Long childId, LocalDate date, Authentication authentication) {
        List<WordEntity> words = dateRangeGeneralService.getByDateAchieveAfter(childId, date, authentication,
                wordsRepository::findByChildIdAndDateAchieveAfter);
        return words.stream()
                .map(getWordsConverter::toDto)
                .collect(Collectors.toList());
    }

    public List<GetWordResponse> getWordsBetweenDays(Long childId, LocalDate startDate, LocalDate endDate, Authentication authentication){
        List<WordEntity> words = dateRangeGeneralService.getWordsBetweenDays(childId, startDate, endDate, authentication,
                wordsRepository::findByChildIdAndDateAchieveBetween);
        return words.stream()
                .map(getWordsConverter::toDto)
                .collect(Collectors.toList());
    }

    public GetWordResponse getByWord(String word) {
        return wordsRepository.findByWord(word)
                .map(getWordsConverter::toDto)
                .orElseThrow(() -> new EntityNotFoundException());
    }

    public GetWordResponse getWordById(long id) {
        return wordsRepository.findById(id)
                .map(getWordsConverter::toDto)
                .orElseThrow(() -> new NoSuchElementException("Word not found with id: " + id));
    }

    public GetAllWordsResponse getAllWords(Long childId, Authentication authentication) {
        authorizationHelper.validateAndAuthorizeChild(childId, authentication);
        return GetAllWordsResponse.builder()
                .words(wordsRepository.findAllByChildId(childId).stream()
                        .map(getWordsConverter::toDto)
                        .collect(Collectors.toList()))
                .build();
    }
}
