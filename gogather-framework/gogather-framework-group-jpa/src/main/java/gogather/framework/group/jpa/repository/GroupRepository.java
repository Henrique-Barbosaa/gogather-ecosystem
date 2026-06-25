package gogather.framework.group.jpa.repository;

import gogather.framework.group.jpa.domain.BaseGroup;
import org.springframework.stereotype.Repository;

//repositório genérico que o framework vai usar internamente
@Repository
public interface GroupRepository extends BaseGroupRepository<BaseGroup> {
}
