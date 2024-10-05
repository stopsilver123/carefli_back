package gradproject.carefli.oauth.jwt;

import gradproject.carefli.oauth.errorHandler.CustomJwtAuthenticationEntryPoint;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomJwtAuthenticationEntryPoint customJwtAuthenticationEntryPoint;
    // 필터 건너뛰는 api url (사용자 인증 미필요 api)
    private static final List<String> EXCLUDED_URLS = Arrays.asList(
            "/login/oauth2/kakao", // 로그인
            "/api/oauth/reissue",  // 액세스 토큰 재발급
            "/gifts/list",
            "/gifts/details",
            "/gifts/recommended/save",
            "/gifts/like",
            "/gifts/recommendations/**"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        //URL이 제외된 경우 다음 필터로 넘어감
        if (isExcludedUrl(requestURI)){
            filterChain.doFilter(request, response);
            return;
        }

        //1. 헤더에서 JWT 받아옴
        String accessToken = jwtService.getTokenFromRequest(request);

        //2. 토큰 유효성 검사
        String validation = "empty";
        if (accessToken != null){
            validation = jwtService.validateToken(accessToken);
        }

        // 3-1. 토큰이 유효한 경우
        if (accessToken != null && validation.equals("IS_VALID")) {

            String userId = jwtService.extractSubject(accessToken);
            // userId로 authentication 객체 생성 -> principal에 유저 정보 담음
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId, null, null);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            // 생성한 authentication을 SecurityContext에 저장
            // token이 인증된 상태를 유지하도록 context(맥락)을 유지
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 다음 필터로 요청 전달 호출
            // UsernamePasswordAuthenticationFilter 로 이동
            filterChain.doFilter(request, response);
        }

        if (validation.equals("empty")){
            // 다음 필터로 요청 전달 호출
            // UsernamePasswordAuthenticationFilter 로 이동
            filterChain.doFilter(request, response);
        }

        if (validation.equals("TOKEN_EXPIRED"))  // 만료
            customJwtAuthenticationEntryPoint.commence(request, response, "TOKEN_EXPIRED");
        else if (validation.equals("INVALID_TOKEN"))  // 유효하지 않음
            customJwtAuthenticationEntryPoint.commence(request, response, "INVALID_TOKEN");
        else if (validation.equals("FAIL_AUTHENTICATION")){  // 인증 실패
            customJwtAuthenticationEntryPoint.commence(request, response, "FAIL_AUTHENTICATION");
        }

    }

    // pathParameter 포함하는 URL 패턴 처리하기 위한 메서드
    private boolean isExcludedUrl(String requestURI) {
        for (String pattern : EXCLUDED_URLS) {
            if (pattern.contains("{")) {
                // 경로 변수 패턴 처리 - 경료변수를 정규 표현식으로 변환하여 매칭
                String regex = pattern.replaceAll("\\{[^/]+\\}", "[^/]+"); // 정규 표현식으로 변환
                if (requestURI.matches(regex)) {
                    return true;
                }
            } else {
                // 단순 경로 매칭 처리
                if (requestURI.startsWith(pattern)) {
                    return true;
                }
            }
        }
        return false;
    }

}
