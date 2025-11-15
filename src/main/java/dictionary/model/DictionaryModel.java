package dictionary.model;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class DictionaryModel implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String BINARY_PATH = "slang.bin";
    private static final String TXT_PATH = "slang.txt";


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

    public void loadDefaultDictionary() {
        String path = TXT_PATH;
        InputStream in = DictionaryModel.class.getClassLoader().getResourceAsStream(path);
        if (in == null) {
            throw new IllegalStateException("Không tìm thấy file " + path + " trong classpath!");
        }

        wordsList.clear();
        wordsHashMap.clear();
        definitionsHashMap.clear();

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
            System.err.println("Send halp to load default dictionary. Message: " + e.getMessage());
        }
        saveBinaryDictionary();
    }

    public void saveBinaryDictionary() {
        String path = BINARY_PATH;
        File file = new File(path);
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
                out.writeObject(this);
            }
        } catch (IOException e) {
            System.err.println("Send halp to save binary dictionary: " + e.getMessage());
        }
    }

    public void loadBinaryDictionary() {
        String path = BINARY_PATH;
        File file = new File(path);

        if (!file.exists()) {
            System.err.println("Send halp to load binary dictionary: file does not exist: " + path);
            return;
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            DictionaryModel loaded = (DictionaryModel) in.readObject();

            this.wordsList.clear();
            this.wordsList.addAll(loaded.wordsList);

            this.wordsHashMap.clear();
            this.wordsHashMap.putAll(loaded.wordsHashMap);

            this.definitionsHashMap.clear();
            this.definitionsHashMap.putAll(loaded.definitionsHashMap);

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Send halp to load binary dictionary. Message: " + e.getMessage());
        }
    }

    public void loadDictionary() {
        File binaryFile = new File(BINARY_PATH);

        if (binaryFile.exists()) {
            try {
                loadBinaryDictionary();
                System.out.println("Loaded dictionary from binary: " + BINARY_PATH);
                return;
            } catch (Exception e) {
                System.err.println("Binary load failed, fall back to txt instead.");
            }
        }

        System.out.println("Binary dictionary not found or failed. Loading default txt...");
        loadDefaultDictionary();
    }


    public DictionaryEntry findWordByName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        DictionaryEntry exact = wordsHashMap.get(name);
        if (exact != null) {
            return exact;
        }

        for (DictionaryEntry entry : wordsHashMap.values()) {
            if (entry.getWord().equalsIgnoreCase(name)) {
                return entry;
            }
        }

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
        if (findWordByName(newWord.getWord()) != null)
            return;

        wordsList.add(newWord);
        wordsHashMap.put(newWord.getWord(), newWord);
        this.addKeywordToDefinitionHashMap(newWord.getDefinition(), newWord.getWord());
        saveBinaryDictionary();
    }

    public void deleteWord(String word) {
        DictionaryEntry oldEntry = findWordByName(word);
        if (oldEntry == null)
            return;

        String exactKey = oldEntry.getWord();
        wordsHashMap.remove(exactKey);

        wordsList.removeIf(entry -> entry.getWord().equalsIgnoreCase(exactKey));

        for (List<String> list : definitionsHashMap.values()) {
            list.removeIf(w -> w.equalsIgnoreCase(exactKey));
        }

        definitionsHashMap.entrySet().removeIf(e -> e.getValue().isEmpty());
        saveBinaryDictionary();
    }

    public void overrideWord(DictionaryEntry newWord) {
        String word = newWord.getWord();

        if (!wordsHashMap.containsKey(word)) {
            addWord(newWord);
            return;
        }

        this.deleteWord(word);
//        DictionaryEntry oldEntry = wordsHashMap.get(word);
//
//        wordsList.removeIf(entry -> entry.getWord().equalsIgnoreCase(word));
//
//        for (List<String> list : definitionsHashMap.values()) {
//            list.removeIf(w -> w.equalsIgnoreCase(word));
//        }
//
//        definitionsHashMap.entrySet().removeIf(e -> e.getValue().isEmpty());

        wordsList.add(newWord);
        wordsHashMap.put(word, newWord);
        this.addKeywordToDefinitionHashMap(newWord.getDefinition(), word);
        saveBinaryDictionary();
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

    public List<DictionaryEntry> get4Words() {
        if (wordsList.size() < 4) {
            return Collections.emptyList();
        }

        List<DictionaryEntry> shuffled = new ArrayList<>(wordsList);
        Collections.shuffle(shuffled, new Random());
        return new ArrayList<>(shuffled.subList(0, 4));
    }

    public DictionaryEntry getRandomWord() {
        if (wordsList.isEmpty()) {
            return null;
        }
        Random random = new Random();
        return wordsList.get(random.nextInt(wordsList.size()));
    }
}
