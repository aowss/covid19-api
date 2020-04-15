package covid19.stats.micasa.com.domain.exceptions;

import java.util.List;
import java.util.stream.Collectors;

public class InvalidFilterException extends RuntimeException{

    List<String> messages;

    public InvalidFilterException(List<String> messages) {
        super(messages != null && !messages.isEmpty() ? messages.stream().collect(Collectors.joining("; ", "[ ", " ]")) : null);
        this.messages = messages;
    }

}
