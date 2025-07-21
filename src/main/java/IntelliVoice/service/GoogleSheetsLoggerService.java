package IntelliVoice.service;

import IntelliVoice.model.BookingLogData;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;

import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@Service
public class GoogleSheetsLoggerService {

    private static final String APPLICATION_NAME = "Formi Voice Logger";
    private static final String SPREADSHEET_ID = "1OlxrmV284ts0J-2H28kXuC4Igj3WlxitNPAF_L9emC4";
    private static final String DEFAULT_RANGE = "Sheet1!A:I"; // ✅ Changed to Sheet1 and A to I

    private Sheets sheetsService;

    public GoogleSheetsLoggerService() throws IOException, GeneralSecurityException {
        sheetsService = getSheetsService();
    }

    private Sheets getSheetsService() throws IOException, GeneralSecurityException {
        GoogleCredential credential = GoogleCredential.fromStream(
                        new FileInputStream("src/main/resources/credentials/sheets-credentials.json"))
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

        return new Sheets.Builder(
                com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport(),
                com.google.api.client.json.jackson2.JacksonFactory.getDefaultInstance(),
                credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    // ✅ Logging chunks from /rooms/logged
    public void logToSheet(String sessionId, String query, List<String> chunks) {
        try {
            List<List<Object>> values = new ArrayList<>();

            for (String chunk : chunks) {
                List<Object> row = Arrays.asList(
                        sessionId,
                        query,
                        chunk,
                        new Date().toString()
                );
                values.add(row);
            }

            ValueRange body = new ValueRange().setValues(values);

            sheetsService.spreadsheets().values()
                    .append(SPREADSHEET_ID, DEFAULT_RANGE, body)
                    .setValueInputOption("RAW")
                    .execute();

        } catch (Exception e) {
            System.err.println("❌ Failed to log to Google Sheet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ✅ Fetching past queries by sessionId
    public List<String> getQueriesBySession(String sessionId) {
        List<String> queries = new ArrayList<>();

        try {
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(SPREADSHEET_ID, DEFAULT_RANGE)
                    .execute();

            List<List<Object>> rows = response.getValues();

            if (rows == null || rows.isEmpty()) return queries;

            for (List<Object> row : rows) {
                if (row.size() >= 2 && row.get(0).equals(sessionId)) {
                    queries.add(row.get(1).toString());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return queries;
    }

    // ✅ Logging Booking Log Data (from /rooms/logbooking)
    public void logBookingCall(BookingLogData data) {
        try {
            List<Object> row = Arrays.asList(
                    data.getCallTime(),
                    data.getPhoneNumber(),
                    data.getCallOutcome(),
                    data.getCheckInDate(),
                    data.getCheckOutDate(),
                    data.getCustomerName(),
                    data.getRoomName(),
                    data.getNumberOfGuests(),
                    data.getCallSummary()
            );

            ValueRange body = new ValueRange()
                    .setValues(Collections.singletonList(row));

            // ✅ Important: Sheet1 must match your tab name
            sheetsService.spreadsheets().values()
                    .append(SPREADSHEET_ID, "Sheet1!A:I", body)
                    .setValueInputOption("RAW")
                    .execute();

        } catch (Exception e) {
            System.err.println("❌ Failed to log booking to Google Sheet: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
