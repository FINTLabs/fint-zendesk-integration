package no.fint;

import com.github.springfox.loader.EnableSpringfox;
import no.fint.cache.annotations.EnableFintCache;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableFintCache
@EnableScheduling
@EnableSpringfox
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
