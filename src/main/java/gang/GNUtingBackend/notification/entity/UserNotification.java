package gang.GNUtingBackend.notification.entity;

import gang.GNUtingBackend.board.entity.BaseTime;
import gang.GNUtingBackend.notification.entity.enums.NotificationStatus;
import gang.GNUtingBackend.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Entity
public class UserNotification extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "user_id")
    @ManyToOne
    private User userId;
    @Column
    private String title;
    @Column
    private String body;
    @Column
    private NotificationStatus status;
    @Column
    private String location;
    @Column
    private Long locationId;

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }
}
