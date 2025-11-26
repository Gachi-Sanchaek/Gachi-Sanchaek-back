package glue.Gachi_Sanchaek.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class KakaoMapApiException extends RuntimeException {
    private final HttpStatus status;

    public KakaoMapApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
