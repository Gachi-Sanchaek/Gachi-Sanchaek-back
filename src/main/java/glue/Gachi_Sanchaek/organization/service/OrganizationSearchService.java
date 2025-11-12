package glue.Gachi_Sanchaek.organization.service;

import glue.Gachi_Sanchaek.organization.dto.OrganizationDTO;
import glue.Gachi_Sanchaek.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizationSearchService {
    private final KakaoMapService kakaoMapService;

    public enum SearchType {
        SENIOR,ANIMAL
    }

    public List<OrganizationDTO> searchNearby(double lat, double lng, int radius, SearchType type){
        List<String> keywords = switch (type){
            case SENIOR -> List.of("노인복지관","노인종합복지관","노인센터","종합사회복지관","노인문화센터");
            case ANIMAL -> List.of("동물보호", "동물보호센터", "유기견 보호소", "보호센터", "입양센터");
        };


        List<OrganizationDTO> allResults = new ArrayList<>();

        for(String keyword : keywords){
            List<OrganizationDTO> result = kakaoMapService.searchNearbyOrganizations(lat, lng, radius, keyword);
            allResults.addAll(result);
        }

        //카카오 아이디로 중복 제거
        List<OrganizationDTO> distinct = allResults.stream()
                .collect(Collectors.toMap(
                        OrganizationDTO::getKakaoId,
                        dto -> dto,
                        (dto1,dto2)-> dto1
                ))
                .values()
                .stream()
                .toList();

        // 불필요한 단어 포함한 경우 삭제
        return distinct.stream()
                .filter(dto -> switch (type){
                    case SENIOR -> isSeniorOrganization(dto);
                    case ANIMAL -> isAnimalShelter(dto);
                })
                .limit(10)
                .collect(Collectors.toList());
    }

    private boolean isSeniorOrganization(OrganizationDTO dto){
        String name = dto.getName();
        if(name == null || name.isEmpty()) return false;

        boolean include = containsAny(name, "복지관", "노인복지", "노인종합복지관");
        boolean exclude = containsAny(name, "요양병원", "요양원", "의원", "병원", "치과","화장실","ATM");

        return include && !exclude;
    }

    private boolean isAnimalShelter(OrganizationDTO dto) {
        String name = dto.getName();
        if (name == null) return false;

        boolean include = containsAny(name, "보호소", "보호센터", "입양센터", "유기동물", "유기견");
        boolean exclude = containsAny(name, "동물병원", "펫샵", "카페", "호텔", "훈련소","노인");

        return include && !exclude;
    }

    private boolean containsAny(String target, String... words){
        return Arrays.stream(words).anyMatch(target::contains);
    }
}
