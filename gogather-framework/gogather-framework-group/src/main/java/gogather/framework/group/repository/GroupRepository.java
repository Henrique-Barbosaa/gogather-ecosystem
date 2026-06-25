package gogather.framework.group.repository;

import gogather.framework.group.domain.BaseGroup;
import org.springframework.stereotype.Repository;

//repositório genérico que o framework vai usar internamente
@Repository
public interface GroupRepository extends BaseGroupRepository<BaseGroup> {
}