package IntelliVoice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingLogData {
    private String callTime;
    private String phoneNumber;
    private String callOutcome;
    private String checkInDate;
    private String checkOutDate;
    private String customerName;
    private String roomName;
    private String numberOfGuests;
    private String callSummary;
}
