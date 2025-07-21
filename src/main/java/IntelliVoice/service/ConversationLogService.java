package IntelliVoice.service;

import IntelliVoice.model.ConversationLog;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ConversationLogService {
    private final Map<String, List<ConversationLog>> conversationMap = new HashMap<>();

    public void saveLog(String sessionId, String query, List<String> chunks) {
        conversationMap
                .computeIfAbsent(sessionId, k -> new ArrayList<>())
                .add(new ConversationLog(sessionId, query, chunks));
    }

    public List<ConversationLog> getLogsBySession(String sessionId) {
        return conversationMap.getOrDefault(sessionId, new ArrayList<>());
    }

    public String summarize(String sessionId) {
        List<ConversationLog> logs = getLogsBySession(sessionId);
        StringBuilder summary = new StringBuilder("Conversation Summary:\n");
        for (ConversationLog log : logs) {
            summary.append("Query: ").append(log.getQuery()).append("\n");
            summary.append("Response: ").append(String.join(" ", log.getResponseChunks())).append("\n\n");
        }
        return summary.toString();
    }
}
