import java.io.Serializable;

public class Message implements Serializable {
    private final String word;
    private final int clock;

    public Message(String word, int clock) {
        this.word = word;
        this.clock = clock;
    }

    public String getWord() {
        return word;
    }

    public int getClock() {
        return clock;
    }
}

