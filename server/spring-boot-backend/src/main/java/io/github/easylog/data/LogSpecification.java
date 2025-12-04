package io.github.easylog.data;

import io.github.easylog.common.Constants;
import io.github.easylog.entity.LogEntity;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Szrnka
 */
public class LogSpecification {

    public static Specification<LogEntity> search(String term, ZonedDateTime startDate, ZonedDateTime endDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (term != null && !term.isBlank()) {
                String like = "%" + term.toLowerCase() + "%";

                predicates.add(
                        cb.or(
                                cb.like(cb.lower(root.get("sessionId")), like),
                                cb.like(cb.lower(root.get("messageId")), like),
                                cb.like(cb.lower(root.get("message")), like),
                                cb.like(cb.lower(root.get("tag")), like),
                                cb.like(cb.lower(root.get("level").as(String.class)), like)
                        )
                );
            }

            if (startDate != null && endDate != null) {
                predicates.add(cb.between(root.get(Constants.TIMESTAMP), startDate, endDate));
            } else  {
                if (startDate != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get(Constants.TIMESTAMP), startDate));
                }

                if (endDate != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get(Constants.TIMESTAMP), endDate));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}