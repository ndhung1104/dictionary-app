package dictionary.model;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DictionaryModel {
    private List<DictionaryEntry> wordsList = new ArrayList<>();
    private Map<String, DictionaryEntry> wordsHashMap = new HashMap<>();
    private Map<String, DictionaryEntry> definitionsHashMap = new HashMap<>();

    public void loadDictionary(String path) {
        InputStream in = DictionaryModel.class.getClassLoader().getResourceAsStream(path);
        if (in == null) {
            throw new IllegalStateException("Không tìm thấy file " + path + " trong classpath!");
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.equals("Slag`Meaning"))
                    continue;
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

    public DictionaryEntry findWordByName(String name) {
        if (wordsHashMap.get(name) != null)
            return wordsHashMap.get(name);

        return null;
    }

    public void addWord(DictionaryEntry newWord) {
        if (wordsHashMap.get(newWord.getWord()) != null)
            return;

        wordsList.add(newWord);
        wordsHashMap.put(newWord.getWord(), newWord);
        // TO DO add definition hashmap
    }

    public List<DictionaryEntry> getWordsList() {
        return wordsList;
    }


}