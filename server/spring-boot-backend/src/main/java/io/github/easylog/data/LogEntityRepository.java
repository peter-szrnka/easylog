package io.github.easylog.data;

import io.github.easylog.entity.LogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * @author Peter Szrnka
 */
@Repository
public interface LogEntityRepository extends JpaRepository<LogEntity, String>, JpaSpecificationExecutor<LogEntity> {
}
