package dictionary.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DictionaryModel {
    private List<DictionaryEntry> wordsList = new ArrayList<>();
    private Map<String, DictionaryEntry> wordsHashMap = new HashMap<>();
    private Map<String, DictionaryEntry> definitionsHashMap = new HashMap<>();

    public void loadDictionary(String path) {

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] lineSplit = line.trim().split("`", 2);
                if (lineSplit.length < 2)
                    continue;
                try {
                    DictionaryEntry newEntry = new DictionaryEntry(lineSplit[0], lineSplit[1]);
                    wordsList.add(newEntry);
                    wordsHashMap.put(lineSplit[0], newEntry);
                    // Add definition lookup later meow
                } catch (IllegalArgumentException e) {
                    System.err.println("Send halp to load dictionary - create new entry. Message: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Send halp to load dictionary. Message: " + e.getMessage());
        }
    }

    public List<DictionaryEntry> getWordsList() {
        return wordsList;
    }


}