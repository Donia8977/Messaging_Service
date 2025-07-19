package com.example.MessageService.security.repository;

import com.example.MessageService.security.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<User> findUsersByCriteria(Map<String, Object> criteria, Long tenantId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> user = query.from(User.class);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(user.get("tenant").get("id"), tenantId));

        if (criteria.get("cities") instanceof Collection<?> cities && !cities.isEmpty()) {
            predicates.add(user.get("city").in(cities));
        }

        if (criteria.get("userTypes") instanceof Collection<?> userTypes && !userTypes.isEmpty()) {
            predicates.add(user.get("type").in(userTypes));
        }

//        if (criteria.get("genders") instanceof Collection<?> genders && !genders.isEmpty()) {
//            // We need to build a list of "OR" conditions for each gender
//            List<Predicate> genderOrPredicates = new ArrayList<>();
//            for (Object genderObj : genders) {
//                if (genderObj instanceof String) {
//                    genderOrPredicates.add(cb.equal(cb.lower(user.get("gender")), ((String) genderObj).toLowerCase()));
//                }
//            }
//            // Add the combined (gender = 'male' OR gender = 'female') predicate to the main list
//            if (!genderOrPredicates.isEmpty()) {
//                predicates.add(cb.or(genderOrPredicates.toArray(new Predicate[0])));
//            }
//        }

        if (criteria.get("age") instanceof Integer && (Integer) criteria.get("age") > 0) {
            predicates.add(cb.greaterThanOrEqualTo(user.get("age"), (Integer) criteria.get("age")));
        }

//        if (criteria.get("gender") instanceof String && !((String) criteria.get("gender")).isEmpty()) {
//            // Compare the strings, ignoring case for robustness
//            predicates.add(cb.equal(cb.lower(user.get("gender")), ((String) criteria.get("gender")).toLowerCase()));
//        }

        query.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<User> findUsersByAnyCriteria(Map<String, Object> criteria, Long tenantId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> user = query.from(User.class);

        List<Predicate> orPredicates = new ArrayList<>();

        if (criteria.get("cities") instanceof Collection<?> cities && !cities.isEmpty()) {
            orPredicates.add(user.get("city").in(cities));
        }

        if (criteria.get("userTypes") instanceof Collection<?> userTypes && !userTypes.isEmpty()) {
            orPredicates.add(user.get("type").in(userTypes));
        }


        if (criteria.get("age") instanceof Integer && (Integer) criteria.get("age") > 0) {
            orPredicates.add(cb.greaterThanOrEqualTo(user.get("age"), (Integer) criteria.get("age")));
        }

//        if (criteria.get("gender") instanceof String && !((String) criteria.get("gender")).isEmpty()) {
//            orPredicates.add(cb.equal(cb.lower(user.get("gender")), ((String) criteria.get("gender")).toLowerCase()));
//        }


        if (orPredicates.isEmpty()) {
            return new ArrayList<>();
        }

        Predicate orClause = cb.or(orPredicates.toArray(new Predicate[0]));

        Predicate tenantClause = cb.equal(user.get("tenant").get("id"), tenantId);

        // The final query is: WHERE tenant_id = ? AND (city IN (...) OR type IN (...) OR ...)
        query.where(cb.and(tenantClause, orClause));

        // Use select distinct to avoid duplicate users if they match multiple criteria
        query.select(user).distinct(true);

        return entityManager.createQuery(query).getResultList();
    }
}