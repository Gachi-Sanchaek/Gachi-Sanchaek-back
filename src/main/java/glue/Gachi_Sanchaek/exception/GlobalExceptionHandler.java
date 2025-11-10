package glue.Gachi_Sanchaek.exception;

import static glue.Gachi_Sanchaek.util.ApiResponse.internalServerError;

import glue.Gachi_Sanchaek.util.ApiResponse;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Hidden
@RestControllerAdvice(basePackages = "glue.Gachi_Sanchaek")
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        return internalServerError(e.getMessage());
    }
}
