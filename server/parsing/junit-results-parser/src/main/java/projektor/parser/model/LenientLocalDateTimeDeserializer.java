package projektor.parser.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

/**
 * Some JUnit XML producers (e.g. pytest 8.3+) write the testsuite {@code timestamp} attribute
 * with a UTC offset, e.g. "2025-08-28T20:34:02.247682+00:00", which {@link LocalDateTime} alone
 * cannot parse. Falls back to parsing as an {@link OffsetDateTime} and dropping the offset when
 * a plain local date-time can't be parsed.
 */
public class LenientLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String text = jsonParser.getText();

        if (text == null || text.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(text);
        } catch (DateTimeParseException e) {
            return OffsetDateTime.parse(text).toLocalDateTime();
        }
    }
}
