package Spring.Practice.repository;

import Spring.Practice.domain.Order;
import Spring.Practice.util.OrderSearch;
import Spring.Practice.util.SimpleOrderQueryDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
public class OrderRepository {

	@PersistenceContext
	private EntityManager em;

	public void save(Order order) {
		em.persist(order);
	}

	public Order findOne(Long orderId) {
		return em.find(Order.class, orderId);
	}

	// 검색 기능 추가(JPQL)
	public List<Order> findAllByString(OrderSearch orderSearch) {
		String query = "select o from Order o join o.member m";
		boolean isFirstCondition = true;

		// 주문 상태로 검색하는 경우(ORDER, CANCEL 중 하나라도 선택한 경우)
		if (orderSearch.getOrderStatus() != null) {
			if (isFirstCondition) {
				query += " where";
				isFirstCondition = false;
			} else {
				query += " and";
			}
			query += " o.status = :status";	// 주문상태로 검색을 하게 된다.
		}

		// 회원 이름으로 검색하는 경우
		// public static boolean hasText(@Nullable String str) {
		//     return str != null && !str.isBlank();
		// }
		if (StringUtils.hasText(orderSearch.getMemberName())) {
			if (isFirstCondition) {
				query += " where";
				isFirstCondition = false;
			} else {
				query += " and";
			}
			query += " m.name like :name";
		}

		// 위의 두 분기문 → 이름도 설정 안하면서 주문상태 역시 설정 안 한 경우 → "select o from Order o join o.member m"(기본 쿼리문이 실행되어 조회가 되는 것)
		TypedQuery<Order> result = em.createQuery(query, Order.class).setMaxResults(1000);

		if (orderSearch.getOrderStatus() != null) {
			result = result.setParameter("status", orderSearch.getOrderStatus());
		}

		if (StringUtils.hasText(orderSearch.getMemberName())) {
			result = result.setParameter("name", orderSearch.getMemberName().trim());
		}
		return result.getResultList();
	}

	// 페치 조인 최적화
	public List<Order> findAllWithMemberDelivery() {
		return em.createQuery("select o from Order o join fetch o.member m join fetch o.delivery d", Order.class)
				.getResultList();
	}

	// 일대다 컬렉션 조회 및 페이징을 적용한 솔루션
	public List<Order> findAllWithMemberDelivery(int offset, int limit) {
		// Order - Member : 다대일 관계
		// Order - Delivery : 일대일 관계
		// [1]. @XToOne → 다대일/일대일 관계를 모두 페치 조인하기
		return em.createQuery("select o from Order o join fetch o.member m join fetch o.delivery d", Order.class)
				.setFirstResult(offset)
				.setMaxResults(limit)
				.getResultList();
	}

	// JPA에서 DTO로 바로 조회할 수 있도록 하기 위해 만든 DTO
	public List<SimpleOrderQueryDTO> findOrderDtos() {
		return em.createQuery("select new Spring.Practice.util.SimpleOrderQueryDTO(o.id, m.name, o.orderDate, o.status, d.address) from Order o join o.member m join o.delivery d", SimpleOrderQueryDTO.class)
				.getResultList();
	}

	// 다대일/일대일 관계 : 페치 조인 적용
	// 컬렉션 페치 조인
	public List<Order> findAllWithItem() {
		return em.createQuery("select distinct o from Order o join fetch o.member m join fetch o.delivery d join fetch o.orderItems oi join fetch oi.item i", Order.class)
				.getResultList();
	}
}
