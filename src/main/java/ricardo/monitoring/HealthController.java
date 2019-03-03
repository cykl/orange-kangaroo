package ricardo.monitoring;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @RequestMapping("/healthz")
    public ResponseEntity<?> getHealthz() {
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
