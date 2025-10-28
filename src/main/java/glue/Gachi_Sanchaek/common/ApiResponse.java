package glue.Gachi_Sanchaek.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;

@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
@Getter
@AllArgsConstructor
public class ApiResponse<T> {
      private final Boolean isSuccess;
      private final String code;
      private final String message;

      @JsonInclude(JsonInclude.Include.NON_NULL)
      private T result;

      public static <T> ApiResponse<T> onSuccess(T result) {
          return new ApiResponse<>(true, "COMMON200", "성공입니다.", result);
      }

      public static <T> ApiResponse<T> onSuccessWithNoData() {
          return new ApiResponse<>(true, "COMMON200", "성공입니다.", null);
      }

      public static <T> ApiResponse<T> onFailure(String code, String message, T data) {
          return new ApiResponse<>(false, code, message, data);
      }
}
