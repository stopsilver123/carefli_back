package gradproject.carefli.oauth.service;

import gradproject.carefli.oauth.dto.KakaoInfoResponseDto;
import gradproject.carefli.oauth.jwt.JwtService;
import gradproject.carefli.oauth.jwt.JwtTokens;
import gradproject.carefli.user.repository.UserRepository;
import gradproject.carefli.user.service.UserService;
import gradproject.carefli.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuthLoginService {
    private final UserRepository userRepository;
    private final KakaoApiClient kakaoApiClient;
    private final JwtService jwtService;
    private final UserService userService;

    public JwtTokens login(String code) {
        // 인가 코드로 액세스 코드 받아옴
        String kakaoAccessToken = kakaoApiClient.getAccessToken(code);
        // 액세스 토큰으로 사용자 정보 받아옴
        KakaoInfoResponseDto kakaoInfoResponseDto = kakaoApiClient.getUserInfo(kakaoAccessToken);

        // 받아온 정보 중 이메일로 유저 찾기 (없으면 생성 - 유저 등록)
        Long userId = findOrCreateUser(kakaoInfoResponseDto);
        // 액세스 토큰 저장
        saveKakaoAccessToken(userId, kakaoAccessToken);

        // 해당 유저의 jwt 인증 토큰 생성해 반환
        return jwtService.generateJwtToken(userId);
    }


    // 이메일로 유저 찾기 메서드 //
    private Long findOrCreateUser(KakaoInfoResponseDto kakaoInfoResponseDto) {
        return userRepository.findByEmail(kakaoInfoResponseDto.getEmail())
                .map(User::getUserId)
                .orElseGet(() -> newUser(kakaoInfoResponseDto)); // 없으면 유저 생성
    }

    // 유저 생성 메서드 //
    private Long newUser(KakaoInfoResponseDto kakaoInfoResponseDto) {
        String nickname = createInitialNickname(); // 초기 닉네임 생성 ex) 익명12345
        User user = User.builder()
                .email(kakaoInfoResponseDto.getEmail())
                .nickname(nickname)
                .build();
        return userRepository.save(user).getUserId();
    }

    // 초기 닉네임 생성
    private String createInitialNickname(){
        int randomNumber;
        String nickname;
        do {
            randomNumber = (int)(Math.random() * 100000);
            nickname = "케어플리" + randomNumber ;
        } while (userRepository.findByNickname(nickname).isPresent());
        return nickname;
    }


    // 카카오 액세스 토큰 저장 //
    private void saveKakaoAccessToken(Long userId, String kakaoAccessToken){
        User user = userRepository.findByUserId(userId);
        user.updateKakaoAccessToken(kakaoAccessToken);
        userRepository.save(user);
    }

}
