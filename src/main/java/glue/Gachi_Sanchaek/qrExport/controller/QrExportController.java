package glue.Gachi_Sanchaek.qrExport.controller;

import glue.Gachi_Sanchaek.qrExport.service.QrExportService;
import glue.Gachi_Sanchaek.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/admin")
public class QrExportController {

    private final QrExportService qrExportService;

    @GetMapping(value = "/export-organization-csv")
    public ResponseEntity<?> exportOrganizationCsv(
            @RequestParam(value = "orgId") Long organizationId
    ){
        log.info("CSV 추출 요청 시작. 요청된 단일 기관 ID: {}", organizationId);

        try {
            String csvData = qrExportService.exportOrganizationToCsv(organizationId);

            String fileName = "organization_" + organizationId + "_payload.csv";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", fileName);
            headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");

            return new ResponseEntity<>(csvData, headers, HttpStatus.OK);

        } catch (IllegalArgumentException e) {

            log.warn("CSV 추출 실패 (기관 ID 찾을 수 없음): {}", organizationId);
            return ApiResponse.badRequest(e.getMessage());

        } catch (Exception e) {
            log.error("CSV 데이터 추출 중 치명적인 오류 발생. ID: {}", organizationId, e);
            return ApiResponse.internalServerError("CSV 데이터 추출 중 서버 오류 발생");
        }
    }
}
