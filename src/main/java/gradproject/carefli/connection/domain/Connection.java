package gradproject.carefli.connection.domain;

import gradproject.carefli.global.entity.BaseTimeEntity;
import gradproject.carefli.connection.dto.ConnectionUpdateRequestDto;
import gradproject.carefli.user.domain.MBTI;
import gradproject.carefli.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Connection extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "connectionId", updatable = false)
    private Long connectionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @Column(name = "connectionName", nullable = false, length = 20)
    private String name;

    @Temporal(TemporalType.DATE)
    private Date birthday;

    @Column
    private String connectionImageUrl;

    @Column
    private String interestTag;

    @Enumerated(EnumType.STRING)
    private MBTI mbti;

    @Column(nullable = false, length = 50)
    private String relationship;

    @Builder
    public Connection(User user, String name, Date birthday, String connectionImageUrl, String interestTag, MBTI mbti, String relationship) {
        this.user = user;
        this.name = name;
        this.birthday = birthday;
        this.connectionImageUrl = connectionImageUrl;
        this.interestTag = interestTag;
        this.mbti = mbti;
        this.relationship = relationship;
    }
    public void updateConnection(ConnectionUpdateRequestDto requestDto){
        this.name = requestDto.getConnectionName();
        this.birthday = requestDto.getBirthday();
        this.interestTag = requestDto.getInterestTag();
        this.mbti = requestDto.getMbti();
        this.connectionImageUrl = requestDto.getConnectionImageUrl();
        this.relationship = requestDto.getRelationship();
    }


}

