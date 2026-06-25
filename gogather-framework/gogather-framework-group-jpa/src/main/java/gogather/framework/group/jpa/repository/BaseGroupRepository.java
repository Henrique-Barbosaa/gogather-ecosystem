package gogather.framework.group.jpa.repository;

import gogather.framework.group.jpa.domain.BaseGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import java.util.Optional;

@NoRepositoryBean
public interface BaseGroupRepository<T extends BaseGroup> extends JpaRepository<T, Long> {
    Optional<T> findByInviteCode(String inviteCode);
    boolean existsByInviteCode(String inviteCode);
}
