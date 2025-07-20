package IntelliVoice.controller;

import IntelliVoice.model.Room;
import IntelliVoice.service.ChunkService;
import IntelliVoice.service.ConversationLogService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class RoomInfoController {

    @Autowired
    private ChunkService chunkService;

    @Autowired
    private ConversationLogService logService;

    // Basic filtered API (no logging)
    @GetMapping("/rooms")
    public List<String> getFilteredRoomChunks(
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String amenity
    ) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = new ClassPathResource("rooms.json").getInputStream();
        List<Room> rooms = mapper.readValue(inputStream, new TypeReference<List<Room>>() {});

        List<Room> filteredRooms = rooms.stream()
                .filter(room -> {
                    boolean matches = true;
                    if (maxPrice != null) {
                        matches &= room.getPricePerNight() <= maxPrice;
                    }
                    if (amenity != null && !amenity.isEmpty()) {
                        matches &= room.getAmenities().toLowerCase().contains(amenity.toLowerCase());
                    }
                    return matches;
                })
                .collect(Collectors.toList());

        return chunkService.chunkRoomInfoSmart(filteredRooms);
    }

    // Same as above, but with logging for conversation tracking
    @GetMapping("/rooms/logged")
    public List<String> getFilteredRoomChunksLogged(
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String amenity,
            @RequestParam String sessionId,
            @RequestParam String query
    ) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = new ClassPathResource("rooms.json").getInputStream();
        List<Room> rooms = mapper.readValue(inputStream, new TypeReference<List<Room>>() {});

        List<Room> filteredRooms = rooms.stream().filter(room -> {
            boolean matches = true;
            if (maxPrice != null) {
                matches &= room.getPricePerNight() <= maxPrice;
            }
            if (amenity != null && !amenity.isEmpty()) {
                matches &= room.getAmenities().toLowerCase().contains(amenity.toLowerCase());
            }
            return matches;
        }).collect(Collectors.toList());

        List<String> chunks = chunkService.chunkRoomInfoSmart(filteredRooms);

        logService.saveLog(sessionId, query, chunks);
        return chunks;
    }

    // Summary endpoint
    @GetMapping("/rooms/summary")
    public String getSummary(@RequestParam String sessionId) {
        return logService.summarize(sessionId);
    }
}
