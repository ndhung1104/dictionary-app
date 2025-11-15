package dictionary.controller;
import java.util.*;
import java.util.stream.Collectors;

import dictionary.model.*;
import dictionary.view.DictionaryView;
import javafx.scene.control.Alert;

public class DictionaryController {
    private DictionaryModel dm;
    private DictionaryView dv;

    public DictionaryController(DictionaryModel dm, DictionaryView dv) {
        this.dm = dm;
        this.dv = dv;
    }

    public void initialize() {
        this.dm.loadDictionary();
    }
    public void reloadDictionary() {
        this.dm.loadDefaultDictionary();
    }

    public void printAll() {
        List<DictionaryEntry> lst = dm.getWordsList();
        for (DictionaryEntry de : lst) {
            System.out.println(de.getWord());
        }

        System.out.println("Number of words: " + Integer.toString(lst.size()));
    }

    public void onEditWord(String word) {
        if (word == null || word.isBlank()) {
            return;
        }
        word = word.trim();

        DictionaryEntry wordEntry = dm.findWordByName(word);
        if (wordEntry != null) {
            dv.showWordOnEditScreen(wordEntry);
        }

    }

    public void onSearchWord(String word) {
//        System.out.println("From controller: " + word);
        if (word == null || word.isBlank()) {
            dv.showMessage("Please enter a word");
            return;
        }
        word = word.trim();

        DictionaryEntry wordEntry = dm.findWordByName(word);
        if (wordEntry == null)  {
//            System.out.println("No word found");
            dv.showMessage("Word not found!");
        } else {
//            System.out.println("Found word");
            dv.showWord(wordEntry);
        }
    }

    public List<String> getWordNamesSorted(){
        return dm.getWordNamesSorted();
    }



    public void onSearchDefinition(String keywords) {
        if (keywords == null || keywords.isBlank()) {
            dv.showMessage("Please enter at least 1 keyword");
            return;
        }
        keywords = keywords.trim();

        List<String> uniqueInputKeywords = Arrays.stream(keywords.split(" "))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        if (uniqueInputKeywords.isEmpty()) {
            dv.showMessage("Please enter valid keywords.");
            return;
        }

        Set<String> commonWords = null;

        for (String keyword : uniqueInputKeywords) {

            String[] resultsFromModel = dm.findWordByDefinitionKeyword(keyword);

            Set<String> currentWords = new HashSet<>(Arrays.asList(resultsFromModel));

            if (commonWords == null) {
                commonWords = currentWords;
            } else {
               commonWords.retainAll(currentWords);
            }

            if (commonWords.isEmpty()) {
                break;
            }
        }

        if (commonWords == null || commonWords.isEmpty()) {
            dv.showMessage("No slang words found that match ALL keywords.");
        } else {
            List<String> finalResults = new ArrayList<>(commonWords);
            for (String w : finalResults) {
                System.out.println(w);
            }
            dv.showDefinition(finalResults);
        }
    }

    public void onAddWord(String word, String definition){
        if (word == null || word.isBlank() || definition == null || definition.isBlank()) {
            dv.showAlert(Alert.AlertType.ERROR, "Error", "Word or definition cannot be blank!");
            return;
        }
        word = word.trim();
        definition = definition.trim();
        if (word.contains(" ") || word.contains("`")){
            dv.showAlert(Alert.AlertType.ERROR, "Error", "Word cannot contain space or ` character!");
            return;
        }
        if (definition.contains("`")){
            dv.showAlert(Alert.AlertType.ERROR, "Error", "Definition cannot contain ` character!");
            return;
        }

        DictionaryEntry wordEntry = dm.findWordByName(word);

        if (wordEntry == null){
            DictionaryEntry newWord = new DictionaryEntry(word, definition);
            dm.addWord(newWord);
            dv.showAlert(Alert.AlertType.INFORMATION, "Success", "Word added successfully!");
            dv.onWordAdded(newWord.getWord());
        } else {
            dv.showDuplicatedWordAlert(wordEntry);
        }
    }

    public void onOverrideWord(DictionaryEntry newWord){
        dm.overrideWord(newWord);
    }

    public void onDuplicateWord(DictionaryEntry newWord) {

        int count = 1;
        String newWordName = newWord.getWord() + "(" + count + ")";
        while (dm.findWordByName(newWordName) != null) {
            count++;
            newWordName = newWord.getWord() + "(" + count + ")";
        }
        DictionaryEntry newWordWithNewName = new DictionaryEntry(newWordName, newWord.getDefinition());
        dm.addWord(newWordWithNewName);
        dv.onWordAdded(newWordName);
    }

    public void onDeleteWord(String word) {
        dm.deleteWord(word);
        dv.onWordDeleted(word);
    }

    public void onUpdateWord(String selectedWord, String updatedWord, String updatedDefinition) {
        if (selectedWord == null || selectedWord.isBlank()) {
            dv.showAlert(Alert.AlertType.ERROR, "Error", "Please select a word to edit.");
            return;
        }

        String originalWord = selectedWord.trim();

        if (updatedWord == null || updatedWord.isBlank() || updatedDefinition == null || updatedDefinition.isBlank()) {
            dv.showAlert(Alert.AlertType.ERROR, "Error", "Word or definition cannot be blank!");
            return;
        }

        updatedWord = updatedWord.trim();
        updatedDefinition = updatedDefinition.trim();

        if (updatedWord.contains(" ") || updatedWord.contains("`")) {
            dv.showAlert(Alert.AlertType.ERROR, "Error", "Word cannot contain space or ` character!");
            return;
        }

        if (updatedDefinition.contains("`")) {
            dv.showAlert(Alert.AlertType.ERROR, "Error", "Definition cannot contain ` character!");
            return;
        }

        DictionaryEntry existingEntry = dm.findWordByName(originalWord);
        if (existingEntry == null) {
            dv.showAlert(Alert.AlertType.ERROR, "Error", "Selected word no longer exists. Please reload the list.");
            return;
        }

        DictionaryEntry duplicateEntry = dm.findWordByName(updatedWord);
        if (duplicateEntry != null && !duplicateEntry.getWord().equals(originalWord)) {
            dv.showAlert(Alert.AlertType.ERROR, "Error", "Another word with the new name already exists!");
            return;
        }

        DictionaryEntry updatedEntry = new DictionaryEntry(updatedWord, updatedDefinition);

        if (originalWord.equals(updatedWord)) {
            dm.overrideWord(updatedEntry);
        } else {
            dm.deleteWord(originalWord);
            dm.addWord(updatedEntry);
            dv.onWordDeleted(originalWord);
            dv.onWordAdded(updatedWord);
        }

        dv.showAlert(Alert.AlertType.INFORMATION, "Success", "Word updated successfully!");
        dv.showWordOnEditScreen(updatedEntry);
    }

    public void show4WordGame() {
        List<DictionaryEntry> listWord = dm.get4Words();
        if (listWord.isEmpty()) {
            dv.showAlert(Alert.AlertType.INFORMATION, "Not enough words", "Please make sure the dictionary has at least 4 entries to play the game.");
            return;
        }
        Random r = new Random();
        int idx = r.nextInt(listWord.size());
        List<String> listAnswer = new ArrayList<>();
        for (DictionaryEntry i : listWord) {
            listAnswer.add(i.getWord());
        }
        dv.showQuestion(listWord.get(idx).getDefinition(), listAnswer, idx);
    }

    public void show4DefinitionGame() {
        List<DictionaryEntry> listWord = dm.get4Words();
        if (listWord.isEmpty()) {
            dv.showAlert(Alert.AlertType.INFORMATION, "Not enough words", "Please make sure the dictionary has at least 4 entries to play the game.");
            return;
        }
        Random r = new Random();
        int idx = r.nextInt(listWord.size());
        List<String> listDefinition = new ArrayList<>();
        for (DictionaryEntry i : listWord) {
            listDefinition.add(i.getDefinition());
        }
        dv.showQuestion(listWord.get(idx).getWord(), listDefinition, idx);
    }
}


