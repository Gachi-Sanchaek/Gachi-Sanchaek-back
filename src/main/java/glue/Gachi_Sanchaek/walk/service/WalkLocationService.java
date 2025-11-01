package glue.Gachi_Sanchaek.walk.service;

import glue.Gachi_Sanchaek.walk.dto.WalkProgressResponse;
import glue.Gachi_Sanchaek.walk.entity.WalkLocation;
import glue.Gachi_Sanchaek.walk.handler.WalkLocationMessage;
import glue.Gachi_Sanchaek.walk.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class WalkLocationService {
    private final LocationRepository locationRepository;
    //산책 시작 시간
    private final Map<Long, LocalDateTime> startTimeMap = new ConcurrentHashMap<>();

    //마지막 위치 정보
    private final Map<Long, WalkLocation> lastLocationMap = new ConcurrentHashMap<>();

    //총 누적 거리
    private final Map<Long, Double> totalDistanceMap = new ConcurrentHashMap<>();

    // 지구 반지름
    private static final int EARTH_RADIUS_KM = 6371;

    // GPS 노이즈 필터링을 위한 최소 거리: 5미터로 설정
    private static final double MIN_DISTANCE_THRESHOLD_KM = 0.005;

    public WalkProgressResponse updateLocation(WalkLocationMessage message) {
        Long walkId = message.getWalkId();
        double lat = message.getLat();
        double lng = message.getLng();

        WalkLocation walkLocation = WalkLocation.builder()
                .walkId(walkId)
                .latitude(lat)
                .longitude(lng)
                .build();
        locationRepository.save(walkLocation);

        long totalMin = calculateTotalMin(walkId);
        double distanceKm = calculateDistanceKm(walkId, lat, lng);

        // 클라이언트로 보낼 응답 생성
        return WalkProgressResponse.builder()
                .status(200)
                .message("위치 업데이트 성공")
                .walkId(walkId)
                .distanceKm(distanceKm) // 총 누적 거리
                .totalMin(totalMin)     // 총 누적 시간 (분)
                .currentLat(lat)
                .currentLng(lng)
                .build();
    }

    //누적 산책 거리 계산
    private double calculateDistanceKm(Long walkId, double newLat, double newLng) {
        //아직 없었다면 누적거리 초기화
        totalDistanceMap.putIfAbsent(walkId, 0.0);

        //직전 위치를 불러옴
        WalkLocation prevLocation = lastLocationMap.get(walkId);

        if (prevLocation == null) {
            // 산책 시작 후 첫 번째 위치 정보인 경우 - 현재 위치를 직전 위치로 저장하고 0.0 반환
            lastLocationMap.put(walkId, new WalkLocation(null, walkId, newLat, newLng));
            return 0.0;
        }

        // 직전 위치와 현재 위치 사이의 이동 거리 계산
        double distanceIncre = getDistanceProcess(
                prevLocation.getLatitude(), prevLocation.getLongitude(), newLat, newLng
        );

        // GPS 노이즈 필터: 5m보다 큰 경우에만 누적
        if (distanceIncre > MIN_DISTANCE_THRESHOLD_KM) {
            totalDistanceMap.merge(walkId, distanceIncre, Double::sum);
        }

        // 현재 위치를 다음 계산을 위한 직전 위치로 업데이트
        lastLocationMap.put(walkId, new WalkLocation(null, walkId, newLat, newLng));

        // 총 누적 거리 반환
        return totalDistanceMap.get(walkId);
    }
    
    //누적 산책 시간 계산
    private long calculateTotalMin(Long walkId) {
        //아직 없었다면 시작 시간을 현재 시간으로 초기화
        startTimeMap.putIfAbsent(walkId, LocalDateTime.now());

        // 시작 시간과 현재 시간의 차이 계산
        Duration duration = Duration.between(startTimeMap.get(walkId), LocalDateTime.now());
        // 분 단위로 변환
        return duration.toMinutes();
    }

    //두 지점간 최단 거리 계산하는 하버사인 계산 로직
    private double getDistanceProcess(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    //웹소켓 연결 종료 시 호출
    public void finishWalk(Long walkId) {
        startTimeMap.remove(walkId);
        lastLocationMap.remove(walkId);
        totalDistanceMap.remove(walkId);
    }
}
