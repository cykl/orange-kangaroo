package ricardo.monitoring.persistance;

import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.PersistenceException;

import static ricardo.monitoring.persistance.BookData.anyValidBook;


@ExtendWith(SpringExtension.class)
@DataJpaTest
class BookTest implements WithAssertions {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void id_is_automatically_set() {
        var book = anyValidBook();
        var uninitializedId = book.getId();

        entityManager.persistAndFlush(book);

        assertThat(book.getId()).isNotEqualTo(uninitializedId);
    }

    @Test
    void title_must_no_be_null() {
        var book = anyValidBook();
        book.setTitle(null);

        assertThatThrownBy(() -> entityManager.persistAndFlush(book))
                .isInstanceOf(PersistenceException.class);
    }

    @Test
    void author_must_no_be_null() {
        var book = anyValidBook();
        book.setAuthor(null);

        assertThatThrownBy(() -> entityManager.persistAndFlush(book))
                .isInstanceOf(PersistenceException.class);
    }

    @Nested
    @DataJpaTest
    class TableConstraints {

        @Test
        void book_with_same_title_and_author_is_ok() {
            var book = anyValidBook();
            entityManager.persistAndFlush(book);

            var book2 = new Book(book.getTitle(), book.getAuthor());
            entityManager.persistAndFlush(book2);

            assertThat(book.getId()).isNotEqualTo(book2.getId());
        }
    }

    @Nested
    @DataJpaTest
    class TableConfiguration {

        @ParameterizedTest
        @ValueSource(strings = {
                "安部 公房 Abe Kōbō", // Japanese
                "Иво Андрић Ivan Andrić", // Serbian Cyrillic
                "\uD83D\uDE00 \uD83D\uDE01 \uD83D\uDE02 \uD83E\uDD23", // Emoji
        })
        void unicode_compatibility(String author) {
            try (var softly = new AutoCloseableSoftAssertions()) {
                var book = anyValidBook();
                book.setTitle(author);
                entityManager.persistAndFlush(book);

                entityManager.refresh(book);
                softly.assertThat(book.getTitle()).isEqualTo(author);
            }
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "\u00e9", // é NFC code point
                "\u0065\u0301", // é NFD code point
        })
        void unicode_code_points_are_not_normalized(String author) {
            try (var softly = new AutoCloseableSoftAssertions()) {
                var book = anyValidBook();
                book.setTitle(author);
                entityManager.persistAndFlush(book);

                entityManager.refresh(book);
                softly.assertThat(book.getTitle()).isEqualTo(author);
            }
        }
    }
}