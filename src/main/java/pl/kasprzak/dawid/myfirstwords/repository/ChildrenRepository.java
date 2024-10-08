package pl.kasprzak.dawid.myfirstwords.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kasprzak.dawid.myfirstwords.repository.dao.ChildEntity;

import java.util.List;

public interface ChildrenRepository extends JpaRepository<ChildEntity, Long> {
    List<ChildEntity> findByParentId(Long parentId);

}
