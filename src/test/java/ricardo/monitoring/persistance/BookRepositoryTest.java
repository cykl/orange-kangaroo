package ricardo.monitoring.persistance;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ricardo.monitoring.persistance.BookData.anyValidBook;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class BookRepositoryTest implements WithAssertions {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository bookRepository;

    @Nested
    @DataJpaTest
    class FindById {

        @Test
        void returns_the_existing_entry() {
            var originalBook = anyValidBook();

            entityManager.persistAndFlush(originalBook);
            entityManager.detach(originalBook);

            Optional<Book> fetchedBook = bookRepository.findById(originalBook.getId());
            assertThat(fetchedBook)
                    .get()
                    .extracting(Book::getId)
                    .isEqualTo(originalBook.getId());
        }
    }

    @Nested
    @DataJpaTest
    class FindAllByAuthor {

        @Test
        void returns_nothing_when_no_there_is_no_book_from_the_author() {
            var fetchedBooks = bookRepository.findAllByAuthor("foo");

            assertThat(fetchedBooks).isEmpty();
        }

        @Test
        void is_case_sensitive() {
            var book = anyValidBook();
            var lowerCaseAuthor = book.getAuthor() + "e";
            var upperCaseAuthor = book.getAuthor() + "E";
            book.setAuthor(lowerCaseAuthor );

            entityManager.persistAndFlush(book);

            var fetchedBooks = bookRepository.findAllByAuthor(upperCaseAuthor);
            assertThat(fetchedBooks).isEmpty();
        }

        @Test
        void returns_all_entries_when_there_are_several_books_from_the_author() {
            var author = UUID.randomUUID().toString();

            var books = List.of(anyValidBook(), anyValidBook());
            books.forEach(book -> {
                book.setAuthor(author);
                entityManager.persistAndFlush(book);
            });

            var fetchedBooks = bookRepository.findAllByAuthor(author);
            assertThat(fetchedBooks).hasSameSizeAs(books);
        }
    }
}
