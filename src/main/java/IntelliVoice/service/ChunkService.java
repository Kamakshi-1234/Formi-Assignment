package IntelliVoice.service;

import IntelliVoice.model.Room;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkService {

    private static final int MAX_TOKENS = 800;
    private static final int APPROX_CHARS_PER_TOKEN = 4;
    private static final int MAX_CHARS = MAX_TOKENS * APPROX_CHARS_PER_TOKEN; // 3200 chars

    public List<String> chunkRoomInfoSmart(List<Room> rooms) {
        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();

        for (Room room : rooms) {
            String roomText = formatRoom(room);


            if (currentChunk.length() + roomText.length() > MAX_CHARS) {
                chunks.add(currentChunk.toString());
                currentChunk = new StringBuilder();
            }

            currentChunk.append(roomText);
        }

        // Add remaining rooms
        if (!currentChunk.isEmpty()) {
            chunks.add(currentChunk.toString());
        }

        return chunks;
    }

    private String formatRoom(Room room) {
        return "Room: " + room.getName() + "\n" +
                "Amenities: " + room.getAmenities() + "\n" +
                "Price: Rs. " + room.getPricePerNight() + "\n\n";
    }
}
