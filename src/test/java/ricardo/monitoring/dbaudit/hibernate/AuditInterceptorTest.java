package ricardo.monitoring.dbaudit.hibernate;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ricardo.monitoring.dbaudit.AuditedOp;
import ricardo.monitoring.dbaudit.Auditor;
import ricardo.monitoring.dbaudit.DbOp;
import ricardo.monitoring.persistance.BookRepository;

import static org.mockito.Mockito.*;
import static ricardo.monitoring.persistance.BookData.anyValidBook;


@ExtendWith(SpringExtension.class)
@SpringBootTest
class AuditInterceptorTest implements WithAssertions {

    @MockBean
    private Auditor auditor;

    @Autowired
    BookRepository bookRepository;

    @Test
    void emit_an_insert_op_when_new_entity_is_saved() {
        var captor = ArgumentCaptor.forClass(AuditedOp.class);

        var book = anyValidBook();
        bookRepository.save(book);

        verify(auditor).audit(captor.capture());
        assertThat(captor.getValue())
                .extracting(AuditedOp::getOp, AuditedOp::getId)
                .containsExactly(DbOp.INSERT, Long.toString(book.getId()));
        verifyNoMoreInteractions(auditor);
    }

    @Test
    void emit_a_delete_op_when_an_entity_is_deleted() {
        var book = anyValidBook();
        bookRepository.save(book);
        bookRepository.delete(book);

        var captor = ArgumentCaptor.forClass(AuditedOp.class);
        var inOrder = inOrder(auditor);
        inOrder.verify(auditor, calls(1)).audit(any(AuditedOp.class));
        inOrder.verify(auditor, calls(1)).audit(captor.capture());
        assertThat(captor.getValue())
                .extracting(AuditedOp::getOp, AuditedOp::getId)
                .containsExactly(DbOp.DELETE, Long.toString(book.getId()));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void emit_an_updated_op_when_an_entity_is_modified() {
        var book = anyValidBook();
        var persistedBook = bookRepository.save(book);

        persistedBook.setTitle("newTitle");
        bookRepository.save(persistedBook);

        var captor = ArgumentCaptor.forClass(AuditedOp.class);
        var inOrder = inOrder(auditor);
        inOrder.verify(auditor, calls(1)).audit(any(AuditedOp.class));
        inOrder.verify(auditor, calls(1)).audit(captor.capture());
        assertThat(captor.getValue())
                .extracting(AuditedOp::getOp, AuditedOp::getId)
                .containsExactly(DbOp.UPDATE, Long.toString(book.getId()));
        inOrder.verifyNoMoreInteractions();
    }
}