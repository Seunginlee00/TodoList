package com.my.boot.user.entity;

import com.my.boot.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

@Table(name = "users")
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE users SET is_delete = true, modified_date = now()  WHERE users_id = ?")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "users_id")
    private Long id; // 고유 번호

    @Column(name = "login_id", nullable = false, unique = true, length = 60)
    private String loginId; // 로그인 아이디

    @Column(name = "user_nm", nullable = false, length = 30)
    private String userNm; // 사용자명

    @Builder.Default
    @Column(name = "admin")
    private int admin = 0; // 	관리자 여부(0: 일반, 1: 관리자 등 )

    @Column(name = "passwd", length = 120)
    private String passwd;  //암호화된 비밀번호.

    @Column(name = "salt", length = 20)
    private String salt;  // 	비밀번호 암호화시 사용되는 salt값

    @Column(name = "email", length = 60)
    private String email; // 이메일

    @Builder.Default
    @Column(name = "mobile_no", length = 100)
    private String mobileNo = "";  //휴대폰 번호.

    @Builder.Default
    @Column(name = "access")
    private int access = 1;  //접근 가능 여부(1: 가능, 0: 정지 등).

    @Column(name = "chosung", length = 10)
    private String chosung; //	이름의 초성(한글검색 최적화에 사용)

    @Builder.Default
    @Column(name = "access_failed_count", nullable = false)
    private int accessFailedCount = 0;  // 로그인 실패 횟수,

    @Column(name = "access_date")
    private LocalDateTime accessDate; // 접근일

    @Builder.Default
    @Column(name = "is_temp_password", nullable = false)
    private int isTempPassword = 0;  // 임시 비밀번호 여부(0: 일반, 1: 임시비밀번호 사용중).

    @Column(name = "passwd_change_date")
    private LocalDateTime passwdChangeDate; // 비밀번호 변경 일시.

    @Column(name = "last_login_date")
    private LocalDateTime lastLoginDate; // 마지막 로그인

    @Builder.Default
    @Column(name = "display", nullable = false)
    private int display = 1;  //화면 표시 여부(1: 표시, 0: 미표시 등).


    @Builder.Default
    @Column(name = "is_delete", nullable = false, columnDefinition = "boolean default false")
    private boolean isDelete = false; // 삭제여부

//    @Builder.Default
//    @OneToMany(mappedBy = "users")
//    List<Board> boardList = new ArrayList<>();


    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserRole> roleList = new ArrayList<>();
}
