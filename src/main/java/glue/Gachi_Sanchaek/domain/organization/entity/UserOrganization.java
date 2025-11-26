package glue.Gachi_Sanchaek.domain.organization.entity;

import glue.Gachi_Sanchaek.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"user", "organization"}, callSuper = false)
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "organization_id"})})
public class UserOrganization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

}
