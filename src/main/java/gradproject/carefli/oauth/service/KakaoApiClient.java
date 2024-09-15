package gradproject.carefli.oauth.service;

import gradproject.carefli.oauth.dto.KakaoInfoResponseDto;
import gradproject.carefli.oauth.dto.KakaoTokens;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class KakaoApiClient {

    @Value("${spring.oauth.kakao.client-id}")
    private String clientId;
    @Value("${spring.oauth.kakao.client-secret}")
    private String clientSecret;
    @Value("${spring.oauth.kakao.url.auth}")
    private String authUri;
    @Value("${spring.oauth.kakao.url.api}")
    private String apiUri;
    @Value("${spring.oauth.kakao.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate;

    // 액세스 토큰 가져오는 메서드 //
    public String getAccessToken(String code) {
        String url = authUri + "/oauth/token";

        // 요청 헤더 설정
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/x-www-form-urlencoded");
        httpHeaders.add("Accept", "application/json");

        // 요청 바디 설정
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code",code);
        body.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request  = new HttpEntity<>(body, httpHeaders);

        try {
            // 인가 코드를 보내 받은 액세스 토큰 응답을 받아옴
            KakaoTokens response = restTemplate.postForObject(url, request, KakaoTokens.class);
            assert response != null;
            return response.getAccessToken();
        } catch (HttpStatusCodeException e) {
            System.err.println("Error response body: " + e.getResponseBodyAsString());
            return null;
        }

    }


    // 사용자 정보 가져오는 메서드 //
    public KakaoInfoResponseDto getUserInfo(String accessToken) {
        String url = apiUri + "/v2/user/me";

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.set("Authorization", "Bearer " + accessToken);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("property_keys", "[\"kakao_account.email\"]");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, httpHeaders);

        return restTemplate.postForObject(url, request, KakaoInfoResponseDto.class);
    }

    // 카카오 회원 탈퇴 메서드 //
    public String UnlinkUser(String accessToken) {
        String unlinkUrl = apiUri +"/v1/user/unlink";

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> request = new HttpEntity<>("", httpHeaders);

        // POST 요청 보내기
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                unlinkUrl,
                HttpMethod.POST,
                request,
                String.class
        );
        return responseEntity.getBody();
    }

}