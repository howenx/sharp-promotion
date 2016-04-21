package mapper;

import domain.*;

import java.util.List;

/**
 * Theme mapper interface.
 * Created by howen on 15/10/26.
 */
public interface ShoppingCartMapper {


    List<OrderLine> selectOrderLine(OrderLine orderLine) throws Exception;

    List<Order> getPinUserOrder(Order order) throws Exception;

}
