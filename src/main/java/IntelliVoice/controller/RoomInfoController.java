package IntelliVoice.controller;

import IntelliVoice.model.Room;
import IntelliVoice.model.BookingLogData;
import IntelliVoice.service.ChunkService;
import IntelliVoice.service.ConversationLogService;
import IntelliVoice.service.GoogleSheetsLoggerService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class RoomInfoController {

    @Autowired
    private ChunkService chunkService;

    @Autowired
    private ConversationLogService logService;

    @Autowired
    private GoogleSheetsLoggerService sheetsLoggerService;

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
        sheetsLoggerService.logToSheet(sessionId, query, chunks);

        return chunks;
    }

    @GetMapping("/rooms/summary")
    public String getSummary(@RequestParam String sessionId) {
        return logService.summarize(sessionId);
    }

    @GetMapping("/rooms/context")
    public List<String> getContextForSession(@RequestParam String sessionId) {
        return sheetsLoggerService.getQueriesBySession(sessionId);
    }

    // ✅ New API for booking log
    @PostMapping("/rooms/logbooking")
    public String logBooking(
            @RequestParam String phoneNumber,
            @RequestParam String callOutcome,
            @RequestParam(defaultValue = "NA") String checkInDate,
            @RequestParam(defaultValue = "NA") String checkOutDate,
            @RequestParam(defaultValue = "Unknown") String customerName,
            @RequestParam(defaultValue = "NA") String roomName,
            @RequestParam(defaultValue = "NA") String numberOfGuests,
            @RequestParam String callSummary
    ) {
        String callTime = ZonedDateTime.now(java.time.ZoneId.of("Asia/Kolkata"))
                .format(DateTimeFormatter.ofPattern("dd MMMM yyyy hh:mm:ss a"));

        BookingLogData data = new BookingLogData(
                callTime,
                phoneNumber,
                callOutcome,
                checkInDate,
                checkOutDate,
                customerName,
                roomName,
                numberOfGuests,
                callSummary
        );

        sheetsLoggerService.logBookingCall(data);
        return "Booking call logged successfully.";
    }
}
