package glue.Gachi_Sanchaek.qrExport.controller;

import glue.Gachi_Sanchaek.docs.SecureOperation;
import glue.Gachi_Sanchaek.qrExport.service.QrExportService;
import glue.Gachi_Sanchaek.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@Tag(name = "QR Export API", description = "기관별 QR코드 이미지 추출 API (관리자용)")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/admin/export-qr")
public class QrExportController {

    private final QrExportService qrExportService;

    @SecureOperation(summary = "단일 기관 QR 이미지 추출", description = "특정 기관의 payload 값으로 생성된 QR 코드를 PNG 이미지로 다운로드합니다.")
    @GetMapping(value = "/organization/image")
    public ResponseEntity<?> exportOrganizationQr(
            @Parameter(description = "Qr 이미지 추출할 기관의 고유 ID")
            @RequestParam(value = "orgId") Long organizationId
    ){
        log.info("QR 이미지 추출 요청 시작. 기관 ID: {}", organizationId);

        try {
            byte[] qrImageBytes = qrExportService.exportOrganizationToQrImage(organizationId);

            String fileName = "organization_" + organizationId + "_qr.png";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);

            headers.setContentDisposition(
                    ContentDisposition.attachment()
                            .filename(fileName, StandardCharsets.UTF_8)
                            .build()
            );

            return new ResponseEntity<>(qrImageBytes, headers, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            log.warn("QR 추출 실패 (잘못된 기관 ID 또는 payload 값 없음): {}", organizationId);
            return ApiResponse.badRequest(e.getMessage());

        } catch (Exception e) {
            log.error("QR 이미지 생성 중 서버 오류 발생. 기관 ID: {}", organizationId, e);
            return ApiResponse.internalServerError("QR 이미지 생성 중 서버 오류가 발생했습니다.");
        }
    }
}
