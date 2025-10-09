package glue.Gachi_Sanchaek.initialization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitializationRunner implements ApplicationRunner {

    private final StampInitializer stampInitializer;

    @Override
    public void run(ApplicationArguments args) {
        stampInitializer.init();
        log.info("Initialization complete.");
    }
}
