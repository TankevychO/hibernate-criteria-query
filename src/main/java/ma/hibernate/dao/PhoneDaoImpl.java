package ma.hibernate.dao;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import ma.hibernate.model.Phone;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class PhoneDaoImpl extends AbstractDao implements PhoneDao {
    public PhoneDaoImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    public Phone create(Phone phone) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = factory.openSession();
            transaction = session.beginTransaction();
            session.save(phone);
            transaction.commit();
            return phone;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Can't add phone to DB: " + phone, e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public List<Phone> findAll(Map<String, String[]> params) {
        try (Session session = factory.openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Phone> phoneQuery = criteriaBuilder.createQuery(Phone.class);
            Root<Phone> phoneRoot = phoneQuery.from(Phone.class);
            Predicate predicate = criteriaBuilder.and();
            for (String param:params.keySet()) {
                CriteriaBuilder.In<String> paramPredicate = criteriaBuilder.in(phoneRoot
                        .get(param));
                for (String value:params.get(param)) {
                    paramPredicate.value(value);
                }
                predicate = criteriaBuilder.and(predicate, paramPredicate);
            }
            phoneQuery.where(predicate);
            return session.createQuery(phoneQuery).getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Can't get phone by parameters: "
                    + params.keySet().stream()
                    .map(key -> key + ": " + Arrays.toString(params.get(key)))
                    .collect(Collectors.toList()), e);
        }
    }
}
