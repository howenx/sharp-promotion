package service;

import domain.Order;
import domain.OrderLine;

import java.util.List;

/**
 * Cart service
 * Created by howen on 15/11/22.
 */
public interface CartService {

    List<OrderLine> selectOrderLine(OrderLine orderLine) throws Exception;

    List<Order> getPinUserOrder(Order order) throws Exception;

    List<Order> getOrder(Order order) throws Exception;

}
