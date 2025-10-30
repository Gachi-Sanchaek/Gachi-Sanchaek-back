package glue.Gachi_Sanchaek.security.jwt;


import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    private final List<String> EXCLUDED_PATHS = List.of(
            "/api/v1/auth",
            "/h2-console",
            "/bonggong",
            "/swagger-ui",
            "/v3/api-docs"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();

        if (EXCLUDED_PATHS.stream().anyMatch(path::startsWith)) { // 토큰 필요 X
            filterChain.doFilter(request, response);
            return;
        }


        String authorization = request.getHeader("Authorization");
        if (isHeaderInvalid(authorization)) {
            request.setAttribute("exception", "Invalid Authorization Header");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String token = authorization.split(" ")[1];

        try{
            Long userId = jwtUtil.getUserId(token);
            String role = jwtUtil.getRole(token);

            //userEntity를 생성하여 값 set
            JWTPayload user = new JWTPayload();
            user.setUserId(userId);
            user.setRole(role);

            CustomUserDetails customUserDetails = new CustomUserDetails(user);
            Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null,
                    customUserDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);

            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            request.setAttribute("exception", "TOKEN_EXPIRED");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
//            throw new BadCredentialsException("JWT token has expired.");
        } catch (JwtException e){
            request.setAttribute("exception", "INVALID_TOKEN");
//            throw new BadCredentialsException("Invalid JWT token.");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private boolean isHeaderInvalid(String authorization) {
        return authorization == null || !authorization.startsWith("Bearer ");
    }

    private boolean isTokenExpired(String token) {
        return jwtUtil.isExpired(token);
    }
}