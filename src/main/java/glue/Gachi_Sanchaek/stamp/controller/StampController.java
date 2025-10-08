package glue.Gachi_Sanchaek.stamp.controller;

import glue.Gachi_Sanchaek.stamp.dto.StampResponseDto;
import glue.Gachi_Sanchaek.stamp.entity.Stamp;
import glue.Gachi_Sanchaek.stamp.service.StampService;
import glue.Gachi_Sanchaek.util.ApiResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/stamp")
public class StampController {

    private final StampService stampService;

    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<StampResponseDto>>> findAllStamps() {
        List<StampResponseDto> result = stampService.findAll().stream().map((StampResponseDto::new)).toList();
        return ApiResponse.ok(result);
    }
}
