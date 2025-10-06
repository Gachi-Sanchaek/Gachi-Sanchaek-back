package glue.Gachi_Sanchaek.security;


import com.fasterxml.jackson.databind.ObjectMapper;
import glue.Gachi_Sanchaek.util.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {


        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        String exception = (String) request.getAttribute("exception");
        String message = "Login is required.";
        if ("TOKEN_EXPIRED".equals(exception)) {
            message = exception;
        }

        log.error("Authentication EntryPoint : {}", message);

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .status(HttpServletResponse.SC_UNAUTHORIZED)
                .message(message)
                .build();

        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(apiResponse));
    }
}
