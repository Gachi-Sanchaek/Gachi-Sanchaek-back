package glue.Gachi_Sanchaek.initialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import glue.Gachi_Sanchaek.stamp.config.BonggongProperties;
import glue.Gachi_Sanchaek.stamp.config.BonggongProperties.Bongogong;
import glue.Gachi_Sanchaek.stamp.entity.Stamp;
import glue.Gachi_Sanchaek.stamp.service.StampService;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StampInitializer {

    private final BonggongProperties bonggongProperties;
    private final StampService stampService;

    public void init(){
        
        // 봉공이 파일명 가져오기
        List<String> filenames;
        try {
            filenames = loadImageFilenames();
        } catch (IOException e) {
            System.out.println("Error loading image files");
            return;
        }

        // yaml에서 봉공이 리스트 가져오기
        List<BonggongProperties.Bongogong> bonggongs = bonggongProperties.getBonggongs();

        List<Stamp> stamps = new ArrayList<>();
        Long index = 1L;
        for (Bongogong bonggong : bonggongs) {
            if(!filenames.contains(bonggong.getFilename())){
                System.out.println("filename not found : "+bonggong.getFilename());
            }
            Stamp stamp = Stamp.builder()
                    .id(index)
                    .name(bonggong.getFilename())
                    .imageUrl("/bonggong/"+bonggong.getFilename())
                    .price((index-1)*500L)
                    .createdAt(LocalDateTime.now())
                    .build();
            stamps.add(stamp);
            index++;
        }
        
        // 저장함수 호출
        stampService.saveAllStamps(stamps);
    }



    public List<String> loadImageFilenames() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:/static/bonggong/*");

        List<String> filenames = new ArrayList<>();
        for (Resource resource : resources) {
            filenames.add(resource.getFilename());
        }

        return filenames;
    }

}
