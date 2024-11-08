package gang.GNUtingBackend.user.dto.token;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LogoutRequestDto {

    private String refreshToken;
    private String fcmToken;
}
