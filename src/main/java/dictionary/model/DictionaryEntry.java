package dictionary.model;

import java.io.Serializable;

public class DictionaryEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private String word;
    private String definition;

    public DictionaryEntry(String word, String definition) {
        if (word == null || word.isBlank()) {
            throw new IllegalArgumentException("Word cannot be null or blank");
        }
        if (definition == null || definition.isBlank()) {
            throw new IllegalArgumentException("Definition cannot be null or blank");
        }
        this.word = word.trim();
        this.definition = definition.trim();
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }
}
