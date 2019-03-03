package ricardo.monitoring.persistance;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Factory methods to help automatic and exploratory testing of book persistance.
 */
final class BookData {

    private static final String[] AUTHORS = {
            "Steve McConnell",
            "Michael T. Nygard",
            "Michael Kerrisk",
            "Tom Demarco",
            "Gerald Weinberg",
    };

    private static final String[] TITLES = {
            "Code complete",
            "The Pragmatic Programmer",
            "Release It!",
            "TCP/IP Illustrated",
            "The Linux Programming Interface",
            "Quality Software Management: Systems thinking",
    };

    private BookData() {
        // Factory
    }

    public static String randomAuthor() {
        var idx = ThreadLocalRandom.current().nextInt(AUTHORS.length);
        return AUTHORS[idx];
    }

    public static String randomTitle() {
        var idx = ThreadLocalRandom.current().nextInt(TITLES.length);
        return TITLES[idx];
    }

    public static Book anyValidBook() {
        return new Book(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
    }
}
