package glue.Gachi_Sanchaek.walkLocation.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import glue.Gachi_Sanchaek.walkLocation.dto.WalkProgressResponse;
import glue.Gachi_Sanchaek.walkLocation.service.WalkLocationService;
import glue.Gachi_Sanchaek.walk.service.WalkRecordService;
import glue.Gachi_Sanchaek.walk.service.WalkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WalkLocationService walkLocationService;
    private final WalkService walkService;
    private final WalkRecordService walkRecordService;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            // 메시지 파싱
            WalkLocationMessage payload = objectMapper.readValue(message.getPayload(), WalkLocationMessage.class);
            Long walkId = payload.getWalkId();

            if (session.getAttributes().get("walkId") == null) {
                session.getAttributes().put("walkId", walkId);
                walkRecordService.onWebSocketConnect(walkId);
                log.info("[WS] 세션 시작: session_id={}, walkId={}", session.getId(), walkId);
            }

            WalkProgressResponse response = walkLocationService.updateLocation(payload);

            // 클라이언트에게 계산된 응답 전송
            String json = objectMapper.writeValueAsString(response);
            session.sendMessage(new TextMessage(json));

        } catch (Exception e) {
            log.error("[WS] 처리 중 오류 발생: {}", e.getMessage(), e);
            try {
                session.sendMessage(new TextMessage("{\"status\":400, \"message\":\"데이터 처리 실패\"}"));
            } catch (Exception ex) {
                log.error("[WS] 오류 응답 전송 실패: {}", ex.getMessage());
            }
        }
    }

    //산책 정상종료 안됐는데 서비스 비정상종료 된 경우
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

        // 세션 속성에 저장해둔 walkId를 가져옵니다.
        Long walkId = (Long) session.getAttributes().get("walkId");

        if (walkId != null) {
            // 서비스의 finishWalk를 호출하여 Map에서 데이터를 삭제합니다.
            walkLocationService.finishWalk(walkId);
            log.info("[WS] 연결 종료 및 메모리 정리: session_id={}, walkId={}", session.getId(), walkId);
        }
        else {
            // walkId가 null인 경우
            log.warn("[WS] 연결 종료. 처리된 walkId가 없습니다: session_id={}", session.getId());
        }
        super.afterConnectionClosed(session, status);
    }
}
