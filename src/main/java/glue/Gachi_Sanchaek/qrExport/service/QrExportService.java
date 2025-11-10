package glue.Gachi_Sanchaek.qrExport.service;

import glue.Gachi_Sanchaek.organization.entity.Organization;
import glue.Gachi_Sanchaek.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QrExportService {
    private final OrganizationRepository organizationRepository;

    /**
     * 특정 기관 ID에 해당하는 정보 (kakao_id, name)를 CSV 형식으로 추출합니다.
     * * @param organizationId 추출할 기관의 DB ID
     * @return CSV 형식의 문자열
     * @throws IllegalArgumentException 해당 ID의 기관을 찾을 수 없을 때 발생
     */

    @Transactional
    public String exportOrganizationToCsv(Long organizationId) {

        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("기관 ID " + organizationId + "에 해당하는 정보를 찾을 수 없습니다."));

        //헤더 정의
        StringBuilder csvContent = new StringBuilder("kakao_id,name\n");

        csvContent
                .append(org.getQrCodePayload()).append(",")
                .append("\"").append(org.getName()).append("\"")
                .append("\n");

        return csvContent.toString();
    }
}
