package IntelliVoice.model;

import java.util.List;

public class ConversationLog {
    private String sessionId;
    private String query;
    private List<String> responseChunks;

    public ConversationLog(String sessionId, String query, List<String> responseChunks) {
        this.sessionId = sessionId;
        this.query = query;
        this.responseChunks = responseChunks;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getQuery() {
        return query;
    }

    public List<String> getResponseChunks() {
        return responseChunks;
    }
}
