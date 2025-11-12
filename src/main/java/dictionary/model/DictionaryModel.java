package dictionary.model;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class DictionaryModel {
    private List<DictionaryEntry> wordsList = new ArrayList<>();
    private Map<String, DictionaryEntry> wordsHashMap = new HashMap<>();
    private Map<String, List<String>> definitionsHashMap = new HashMap<>();

    private void addKeywordToDefinitionHashMap(String definition, String slangWord) {
        String[] definitionList = definition.split("\\s*\\|\\s*");
        for (String definitionElement : definitionList) {
            String cleanDefinition = definitionElement.trim(); // Đảm bảo sạch
            if (cleanDefinition.isEmpty()) {
                continue; // Bỏ qua nếu định nghĩa rỗng
            }

            if (!definitionsHashMap.containsKey(cleanDefinition)) {
                // Nếu định nghĩa này chưa có trong map
                // Tạo một list mới
                List<String> wordsForThisDef = new ArrayList<>();
                // Thêm từ slang hiện tại vào
                wordsForThisDef.add(slangWord);
                // Đặt list mới này vào map
                definitionsHashMap.put(cleanDefinition, wordsForThisDef);
            } else {
                // Nếu định nghĩa này đã có
                // Lấy list cũ ra
                List<String> existingList = definitionsHashMap.get(cleanDefinition);
                if (!existingList.contains(slangWord)) {
                    existingList.add(slangWord);
                }
            }
        }
    }

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
                    wordsHashMap.put(newEntry.getWord(), newEntry);
                    addKeywordToDefinitionHashMap(newEntry.getDefinition(), newEntry.getWord());

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

    public String[] findWordByDefinitionKeyword(String keyword) {
        Set<String> matchingSlangWords = new HashSet<>();
        String lowerCaseKeyword = keyword.toLowerCase(); // Để tìm kiếm không phân biệt hoa/thường

        for (String fullDefinition : definitionsHashMap.keySet()) {

            if (fullDefinition.toLowerCase().contains(lowerCaseKeyword)) {

                List<String> slangWords = definitionsHashMap.get(fullDefinition);

                matchingSlangWords.addAll(slangWords);
            }
        }
        return matchingSlangWords.toArray(new String[0]);
    }

    public void addWord(DictionaryEntry newWord) {
        if (wordsHashMap.get(newWord.getWord()) != null)
            return;

        wordsList.add(newWord);
        wordsHashMap.put(newWord.getWord(), newWord);
        addKeywordToDefinitionHashMap(newWord.getDefinition(), newWord.getWord());
    }

    public List<DictionaryEntry> getWordsList() {
        return wordsList;
    }

    public List<String> getWordNamesSorted() {
        return wordsList.stream()
                .map(DictionaryEntry::getWord)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }


}