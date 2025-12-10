package io.github.easylog.data;

import io.github.easylog.entity.LogEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Szrnka
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LogSpecification {

    private static final String TIMESTAMP = "timestamp";

    public static Specification<LogEntity> search(String term, ZonedDateTime startDate, ZonedDateTime endDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (term != null && !term.isBlank()) {
                String like = "%" + term.toLowerCase() + "%";

                predicates.add(
                        cb.or(
                                cb.like(cb.lower(root.get("sessionId")), like),
                                cb.like(cb.lower(root.get("logEntryId")), like),
                                cb.like(cb.lower(root.get("message")), like),
                                cb.like(cb.lower(root.get("tag")), like),
                                cb.like(cb.lower(root.get("level").as(String.class)), like)
                        )
                );
            }

            if (startDate != null && endDate != null) {
                predicates.add(cb.between(root.get(TIMESTAMP), startDate, endDate));
            } else  {
                if (startDate != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get(TIMESTAMP), startDate));
                }

                if (endDate != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get(TIMESTAMP), endDate));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}