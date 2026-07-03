package gogather.framework.group.jpa.repository;

import gogather.framework.group.jpa.domain.BaseGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@NoRepositoryBean
public interface BaseGroupRepository<T extends BaseGroup> extends JpaRepository<T, Long> {
    Optional<T> findByInviteCode(String inviteCode);
    boolean existsByInviteCode(String inviteCode);

    Optional<T> findByExternalId(UUID externalId);
    boolean existsByExternalId(UUID externalId);

    @Query("SELECT g FROM #{#entityName} g JOIN g.members m WHERE m.user.id = :userId")
    List<T> findGroupsByUserId(@Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM #{#entityName} g JOIN g.members m WHERE g.inviteCode = :inviteCode AND m.user.id = :userId")
    boolean isGroupMemberByInviteCode(@Param("inviteCode") String inviteCode, @Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM #{#entityName} g JOIN g.members m WHERE g.externalId = :externalId AND m.user.id = :userId")
    boolean isGroupMemberByExternalId(@Param("externalId") UUID externalId, @Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM #{#entityName} g JOIN g.members m WHERE g.id = :groupId AND m.user.id = :userId")
    boolean isGroupMemberByUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM #{#entityName} g JOIN g.members m WHERE g.id = :groupId AND m.user.id = :userId")
    boolean isGroupMember(@Param("groupId") Long groupId, @Param("userId") Long userId);
}
