package service;

import domain.Collect;
import domain.Order;
import domain.OrderLine;
import mapper.ShoppingCartMapper;

import javax.inject.Inject;
import java.util.List;

/**
 * 购物车service实现
 * Created by howen on 15/11/22.
 */
public class CartServiceImpl implements CartService {

    @Inject
    private ShoppingCartMapper shoppingCartMapper;

    @Override
    public List<OrderLine> selectOrderLine(OrderLine orderLine) throws Exception {
        return shoppingCartMapper.selectOrderLine(orderLine);
    }

    @Override
    public List<Order> getPinUserOrder(Order order) throws Exception {
        return shoppingCartMapper.getPinUserOrder(order);
    }
}
