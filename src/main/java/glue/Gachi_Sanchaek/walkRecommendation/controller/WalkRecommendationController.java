package glue.Gachi_Sanchaek.walkRecommendation.controller;

import glue.Gachi_Sanchaek.util.ApiResponse;
import glue.Gachi_Sanchaek.organization.entity.Organization;
import glue.Gachi_Sanchaek.walkRecommendation.service.WalkRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class WalkRecommendationController {
    private final WalkRecommendationService walkRecommendationService;


}
