package glue.Gachi_Sanchaek.common.initialization;

import glue.Gachi_Sanchaek.domain.stamp.config.BonggongProperties;
import glue.Gachi_Sanchaek.domain.stamp.config.BonggongProperties.Bongogong;
import glue.Gachi_Sanchaek.domain.stamp.entity.Stamp;
import glue.Gachi_Sanchaek.domain.stamp.service.StampService;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("dev")
public class StampInitializer {

    private static final String STAMP_RESOURCE_PATH = "classpath:/static/bonggong/*";
    private static final String STAMP_IMAGE_URL_PREFIX = "/bonggong/";

    private final BonggongProperties bonggongProperties;
    private final StampService stampService;


    public void init(){
        log.info("Starting Stamp initialization...");

        // 봉공이 파일명 가져오기
        Set<String> filenames;
        try {
            filenames = loadImageFilenames();
        } catch (IOException e) {
            log.error("Fail to load image files from /static/bonggong");
            throw new RuntimeException("error loading image files");
        }

        // yaml에서 봉공이 리스트 가져오기
        List<BonggongProperties.Bongogong> bonggongs = bonggongProperties.getBonggongs();

        List<Stamp> stamps = new ArrayList<>();
        Long index = 1L;
        boolean hasMissingFiles = false;
        LocalDateTime batchCreatedAt = LocalDateTime.now();

        for (Bongogong bonggong : bonggongs) {
            if(!filenames.contains(bonggong.getFilename())){
                log.error("Stamp config error: File '{}' from YAML not found in /static/bonggong/", bonggong.getFilename());
                hasMissingFiles = true;
                continue;
            }
            Stamp stamp = Stamp.builder()
                    .id(index)
                    .name(bonggong.getName())
                    .imageUrl(STAMP_IMAGE_URL_PREFIX+bonggong.getFilename())
                    .price((index-1)*500L)
                    .createdAt(batchCreatedAt)
                    .build();
            stamps.add(stamp);
            index++;
        }

        if (hasMissingFiles) {
            throw new IllegalStateException("Missing stamp image files found. Check application logs. Aborting initialization.");
        }
        
        // 저장함수 호출
        if (!stamps.isEmpty()) {
            stampService.saveAllStamps(stamps);
            log.info("Successfully initialized {} stamps.", stamps.size());
        } else {
            log.warn("No stamps were initialized.");
        }
    }



    public Set<String> loadImageFilenames() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(STAMP_RESOURCE_PATH);

        return Arrays.stream(resources)
                .map(Resource::getFilename)
                .collect(Collectors.toSet());
    }

}
