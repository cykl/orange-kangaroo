package ricardo.monitoring.persistance;


import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.mvc.TypeReferences.ResourceType;
import org.springframework.http.RequestEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

import static org.springframework.data.rest.webmvc.RestMediaTypes.HAL_JSON;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookRestRepositoryTest implements WithAssertions {

    @SpringBootApplication
    static class Config {

        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate(new HttpComponentsClientHttpRequestFactory());
        }
    }

    private static final String SERVICE_URI = "http://localhost:%s/books";

    @LocalServerPort
    int port;

    @Autowired
    RestOperations restOperations;

    @Test
    void simple_create_read_update_scenario() {
        URI uri = URI.create(String.format(SERVICE_URI, port));
        var postReq = RequestEntity
                .post(uri)
                .accept(HAL_JSON)
                .body(
                        Map.of(
                                "author", "Allspaw",
                                "title", "The Art of Capacity Planning"));

        var postResp = restOperations.exchange(postReq, new ResourceType<Book>() {
        }).getBody();
        var bookLink = new Links(postResp.getLinks()).getLink("book");

        var getReq = RequestEntity
                .get(URI.create(bookLink.getHref()))
                .accept(HAL_JSON)
                .build();
        var getResp = restOperations.exchange(getReq, new ResourceType<Book>() {
        }).getBody();
        assertThat(getResp.getContent())
                .extracting(b -> b.author, b -> b.title)
                .containsExactly("Allspaw", "The Art of Capacity Planning");

        var patchReq = RequestEntity
                .patch(URI.create(bookLink.getHref()))
                .body(Map.of("author", "John Allspaw"));
        var patchResp = restOperations.exchange(patchReq, new ResourceType<Book>() {
        }).getBody();
        assertThat(patchResp.getContent())
                .extracting(b -> b.author, b -> b.title)
                .containsExactly("John Allspaw", "The Art of Capacity Planning");
    }

    static class Book {
        public String author;
        public String title;
    }
}
