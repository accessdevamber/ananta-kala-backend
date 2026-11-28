package com.anantakala.backend.repository;

import com.anantakala.backend.entity.Order;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByCart_Id(UUID cartId);

    Optional<Order> findByOrderNumber(String orderNumber);

    boolean existsByCart_Id(UUID cartId);

    @EntityGraph(attributePaths = "items")
    @Query("SELECT o FROM Order o WHERE o.id = :orderId")
    Optional<Order> findByIdWithItemsUsingEntityGraph(
            @Param("orderId") UUID orderId
    );

    @Query("""
        SELECT o
        FROM Order o
        LEFT JOIN FETCH o.items
        WHERE o.id = :orderId
        """)
    Optional<Order> findByIdWithItemsUsingJoinFetch(
            @Param("orderId") UUID orderId
    );

    // Fetch paginated Orders with OrderItems
    @EntityGraph(attributePaths = "items")
    @Query(
            value = """
                    SELECT o
                    FROM Order o
                    """,
            countQuery = """
                    SELECT COUNT(o)
                    FROM Order o
                    """
    )
    Page<Order> findAllWithItems(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT o
        FROM Order o
        WHERE o.id = :orderId
        """)
    Optional<Order> findByIdForUpdate(
            @Param("orderId") UUID orderId
    );

    //====above sql query with pagination=====
    //select
    //        o1_0.id,
    //        o1_0.cart_id,
    //        o1_0.created_at,
    //        o1_0.currency,
    //        o1_0.discount_amount,
    //        i1_0.order_id,
    //        i1_0.id,
    //        i1_0.created_at,
    //        i1_0.frame_finish,
    //        i1_0.line_total,
    //        i1_0.product_name,
    //        i1_0.product_type,
    //        i1_0.product_variant_id,
    //        i1_0.quantity,
    //        i1_0.size_label,
    //        i1_0.sku,
    //        i1_0.unit_price,
    //        i1_0.updated_at,
    //        o1_0.order_number,
    //        o1_0.shipping_amount,
    //        o1_0.status,
    //        o1_0.subtotal,
    //        o1_0.total_amount,
    //        o1_0.updated_at
    //    from
    //        (select
    //            o1_0.id,
    //            o1_0.cart_id,
    //            o1_0.created_at,
    //            o1_0.currency,
    //            o1_0.discount_amount,
    //            o1_0.order_number,
    //            o1_0.shipping_amount,
    //            o1_0.status,
    //            o1_0.subtotal,
    //            o1_0.total_amount,
    //            o1_0.updated_at
    //        from
    //            public.orders o1_0
    //        order by
    //            o1_0.created_at desc,
    //            o1_0.id desc
    //        offset
    //            ? rows
    //        fetch
    //            first ? rows only) o1_0(id, cart_id, created_at, currency, discount_amount, order_number, shipping_amount, status, subtotal, total_amount, updated_at)
    //    left join
    //        public.order_items i1_0
    //            on o1_0.id=i1_0.order_id
    //    order by
    //        o1_0.created_at desc,
    //        o1_0.id desc
    //
    //Hibernate:
    //    select
    //        count(o1_0.id)
    //    from
    //        public.orders o1_0






    //demo
//    @EntityGraph(attributePaths = "items")
//    @Query("SELECT o FROM Order o")
//    List<Order> findAllWithItems();

//    @EntityGraph(attributePaths = "items")
//    @Query(
//            value = "SELECT o FROM Order o",
//            countQuery = "SELECT COUNT(o) FROM Order o"
//    )
//    Page<Order> findAllWithItemsUsingPagination(
//            Pageable pageable
//    );

//    @Query(
//            value = """
//                SELECT o
//                FROM Order o
//                LEFT JOIN FETCH o.items
//                """,
//            countQuery = """
//                SELECT COUNT(o)
//                FROM Order o
//                """
//    )
//    Page<Order> findAllWithItemsUsingJoinFetch(
//            Pageable pageable
//    );

}


//no optimisation
//Order order = orderRepository.findById(orderId) uses 2 below queries
//Hibernate:
//    select
//        o1_0.id,
//        o1_0.cart_id,
//        o1_0.created_at,
//        o1_0.currency,
//        o1_0.discount_amount,
//        o1_0.order_number,
//        o1_0.shipping_amount,
//        o1_0.status,
//        o1_0.subtotal,
//        o1_0.total_amount,
//        o1_0.updated_at
//    from
//        public.orders o1_0
//    where
//        o1_0.id=?
//Hibernate:
//    select
//        i1_0.order_id,
//        i1_0.id,
//        i1_0.created_at,
//        i1_0.frame_finish,
//        i1_0.line_total,
//        i1_0.product_name,
//        i1_0.product_type,
//        i1_0.product_variant_id,
//        i1_0.quantity,
//        i1_0.size_label,
//        i1_0.sku,
//        i1_0.unit_price,
//        i1_0.updated_at
//    from
//        public.order_items i1_0
//    where
//        i1_0.order_id=?


//@EntityGraph(attributePaths = "items")
//    @Query("SELECT o FROM Order o WHERE o.id = :orderId")
//    Optional<Order> findByIdWithItemsUsingEntityGraph(
//            @Param("orderId") UUID orderId
//    );
//Hibernate:
//    select
//        o1_0.id,
//        o1_0.cart_id,
//        o1_0.created_at,
//        o1_0.currency,
//        o1_0.discount_amount,
//        i1_0.order_id,
//        i1_0.id,
//        i1_0.created_at,
//        i1_0.frame_finish,
//        i1_0.line_total,
//        i1_0.product_name,
//        i1_0.product_type,
//        i1_0.product_variant_id,
//        i1_0.quantity,
//        i1_0.size_label,
//        i1_0.sku,
//        i1_0.unit_price,
//        i1_0.updated_at,
//        o1_0.order_number,
//        o1_0.shipping_amount,
//        o1_0.status,
//        o1_0.subtotal,
//        o1_0.total_amount,
//        o1_0.updated_at
//    from
//        public.orders o1_0
//    left join
//        public.order_items i1_0
//            on o1_0.id=i1_0.order_id
//    where
//        o1_0.id=?


//@Query("""
//        SELECT o
//        FROM Order o
//        LEFT JOIN FETCH o.items
//        WHERE o.id = :orderId
//        """)
//    Optional<Order> findByIdWithItemsUsingJoinFetch(
//            @Param("orderId") UUID orderId
//    );
//Hibernate:
//    select
//        o1_0.id,
//        o1_0.cart_id,
//        o1_0.created_at,
//        o1_0.currency,
//        o1_0.discount_amount,
//        i1_0.order_id,
//        i1_0.id,
//        i1_0.created_at,
//        i1_0.frame_finish,
//        i1_0.line_total,
//        i1_0.product_name,
//        i1_0.product_type,
//        i1_0.product_variant_id,
//        i1_0.quantity,
//        i1_0.size_label,
//        i1_0.sku,
//        i1_0.unit_price,
//        i1_0.updated_at,
//        o1_0.order_number,
//        o1_0.shipping_amount,
//        o1_0.status,
//        o1_0.subtotal,
//        o1_0.total_amount,
//        o1_0.updated_at
//    from
//        public.orders o1_0
//    left join
//        public.order_items i1_0
//            on o1_0.id=i1_0.order_id
//    where
//        o1_0.id=?


//==============DEMO==============
//List<Order> orders = orderRepository.findAll();this is for demo have 3 orders in db
//Hibernate:
//    select
//        o1_0.id,
//        o1_0.cart_id,
//        o1_0.created_at,
//        o1_0.currency,
//        o1_0.discount_amount,
//        o1_0.order_number,
//        o1_0.shipping_amount,
//        o1_0.status,
//        o1_0.subtotal,
//        o1_0.total_amount,
//        o1_0.updated_at
//    from
//        public.orders o1_0
//2026-11-26T16:45:13.338+05:30  INFO 51685 --- [ananta-kala-backend] [nio-9191-exec-8] c.a.backend.service.OrderService         : Total Orders: 3
//Hibernate:
//    select
//        i1_0.order_id,
//        i1_0.id,
//        i1_0.created_at,
//        i1_0.frame_finish,
//        i1_0.line_total,
//        i1_0.product_name,
//        i1_0.product_type,
//        i1_0.product_variant_id,
//        i1_0.quantity,
//        i1_0.size_label,
//        i1_0.sku,
//        i1_0.unit_price,
//        i1_0.updated_at
//    from
//        public.order_items i1_0
//    where
//        i1_0.order_id=?
//2026-11-26T16:45:13.346+05:30  INFO 51685 --- [ananta-kala-backend] [nio-9191-exec-8] c.a.backend.service.OrderService         : Order ID: 01a2128f-0a81-77ce-9c70-a5baba89c8e0, Number of items: 1
//Hibernate:
//    select
//        i1_0.order_id,
//        i1_0.id,
//        i1_0.created_at,
//        i1_0.frame_finish,
//        i1_0.line_total,
//        i1_0.product_name,
//        i1_0.product_type,
//        i1_0.product_variant_id,
//        i1_0.quantity,
//        i1_0.size_label,
//        i1_0.sku,
//        i1_0.unit_price,
//        i1_0.updated_at
//    from
//        public.order_items i1_0
//    where
//        i1_0.order_id=?
//2026-11-26T16:45:13.347+05:30  INFO 51685 --- [ananta-kala-backend] [nio-9191-exec-8] c.a.backend.service.OrderService         : Order ID: 01a21705-856c-7a0c-b0df-9ec7a9bc5e14, Number of items: 1
//Hibernate:
//    select
//        i1_0.order_id,
//        i1_0.id,
//        i1_0.created_at,
//        i1_0.frame_finish,
//        i1_0.line_total,
//        i1_0.product_name,
//        i1_0.product_type,
//        i1_0.product_variant_id,
//        i1_0.quantity,
//        i1_0.size_label,
//        i1_0.sku,
//        i1_0.unit_price,
//        i1_0.updated_at
//    from
//        public.order_items i1_0
//    where
//        i1_0.order_id=?


//    @EntityGraph(attributePaths = "items")
//    @Query("SELECT o FROM Order o")
//    List<Order> findAllWithItems();
//demo with enitygraph only single query
//Hibernate:
//    select
//        o1_0.id,
//        o1_0.cart_id,
//        o1_0.created_at,
//        o1_0.currency,
//        o1_0.discount_amount,
//        i1_0.order_id,
//        i1_0.id,
//        i1_0.created_at,
//        i1_0.frame_finish,
//        i1_0.line_total,
//        i1_0.product_name,
//        i1_0.product_type,
//        i1_0.product_variant_id,
//        i1_0.quantity,
//        i1_0.size_label,
//        i1_0.sku,
//        i1_0.unit_price,
//        i1_0.updated_at,
//        o1_0.order_number,
//        o1_0.shipping_amount,
//        o1_0.status,
//        o1_0.subtotal,
//        o1_0.total_amount,
//        o1_0.updated_at
//    from
//        public.orders o1_0
//    left join
//        public.order_items i1_0
//            on o1_0.id=i1_0.order_id
