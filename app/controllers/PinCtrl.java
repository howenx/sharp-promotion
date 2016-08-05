package controllers;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import domain.*;
import filters.UserAuth;
import net.spy.memcached.BuildInfo;
import net.spy.memcached.MemcachedClient;
import org.apache.commons.beanutils.BeanUtils;
import play.Application;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import service.CartService;
import service.IdService;
import service.PromotionService;
import service.SkuService;
import util.CalCountDown;
import util.GenCouponCode;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static play.libs.Json.newObject;
import static util.SysParCom.*;

/**
 * 拼购
 * Created by howen on 16/2/17.
 */
public class PinCtrl extends Controller {

    @Inject
    private SkuService skuService;

    @Inject
    private CartService cartService;

    @Inject
    private IdService idService;

    @Inject
    private PromotionService promotionService;


    @Inject
    private ActorSystem system;

    //将Json串转换成List
    private static ObjectMapper mapper = new ObjectMapper();

    @Inject
    private MemcachedClient cache;

    /**
     * 拼购活动页
     *
     * @param activityId 活动ID
     * @param pay        是否开团页
     * @return json
     */
    public Result pinActivity(Long activityId, Integer pay, Integer userPayType) {

        ObjectNode result = newObject();

        Optional<String> header = Optional.ofNullable(request().getHeader("id-token"));

        try {

            PinActivityDTO pinActivityDTO = new PinActivityDTO();

            PinActivity pinActivity = promotionService.selectPinActivityById(activityId);

            if (pinActivity != null) {
                BeanUtils.copyProperties(pinActivityDTO, pinActivity);

                PinUser pinUser = new PinUser();
                pinUser.setPinActiveId(pinActivity.getPinActiveId());
                List<PinUser> pinUserList = promotionService.selectPinUser(pinUser);

                pinUserList = pinUserList.stream().map(p -> {
                    p.setUserImg(IMAGE_URL + p.getUserImg());
                    try {
                        ID userNm = idService.getID(p.getUserId());
                        if (userNm == null)
                            p.setUserNm(("HMM-RB" + GenCouponCode.GetCode(4)).toLowerCase());
                        else p.setUserNm(idService.getID(p.getUserId()).getNickname());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return p;
                }).collect(Collectors.toList());

                pinActivityDTO.setPinUsers(pinUserList);

                if (pay == 2) {
                    pinActivityDTO.setPay("new");
                    if (userPayType == 1) pinActivityDTO.setUserType("master");
                    else pinActivityDTO.setUserType("ordinary");
                } else pinActivityDTO.setPay("normal");

                pinActivityDTO.setPinUrl(PROMOTION_URL + "/promotion/pin/activity/" + activityId);

                pinActivityDTO.setEndCountDown(CalCountDown.getEndTimeSubtract(pinActivityDTO.getEndAt()));

                PinSku pinSku = promotionService.getPinSkuById(pinActivityDTO.getPinId());


                Sku sku = new Sku();
                sku.setId(pinSku.getInvId());
                sku = skuService.getInv(sku);
                pinActivityDTO.setPinSkuUrl(DEPLOY_URL + "/comm/detail/pin/" + sku.getItemId() + "/" + pinSku.getPinId());
                pinActivityDTO.setPinTitle(pinSku.getPinTitle());


                JsonNode js_invImg = Json.parse(pinSku.getPinImg());
                if (js_invImg.has("url")) {
                    ((ObjectNode) js_invImg).put("url", IMAGE_URL + js_invImg.get("url").asText());
                }
                pinActivityDTO.setPinImg(js_invImg.toString());

                //用户状态
                if (header.isPresent()) {
                    Optional<Object> token = Optional.ofNullable(cache.get(header.get()));
                    if (token.isPresent()) {
                        JsonNode userJson = Json.parse(token.get().toString());
                        Long userId = Long.valueOf(userJson.findValue("id").asText());
                        PinUser pu = new PinUser();
                        pu.setUserId(userId);
                        pu.setPinActiveId(activityId);
                        List<PinUser> pinUsers = promotionService.selectPinUser(pu);
                        if (pinUsers.size() > 0) {
                            pinActivityDTO.setOrJoinActivity(1);
                        } else {//此用户没有加入活动,则需要先判断此活动的状态,如果活动为C或者F状态,那么就需要返回E状态,告知用户此拼购已经结束了,并返回orderLine表中的

                            if (pinActivity.getStatus().equals("C") || pinActivity.getStatus().equals("F")) {
                                pinActivityDTO.setStatus("E");
                                result.putPOJO("themeList", Json.toJson(getPushPin()));
                            }
                            pinActivityDTO.setOrJoinActivity(0);
                        }

                        Integer userPin = 0;

                        //用户存在,需要验证用户是否符合限购策略
                        Order order = new Order();
                        order.setOrderType(2);//拼购订单
                        order.setUserId(userId);
                        List<Order> orders = cartService.getPinUserOrder(order);
                        if (orders.size() > 0) {
                            for (Order os : orders) {
                                OrderLine orderLine = new OrderLine();
                                orderLine.setOrderId(os.getOrderId());
                                orderLine.setSkuType("pin");
                                orderLine.setSkuTypeId(pinSku.getPinId());
                                List<OrderLine> lines = cartService.selectOrderLine(orderLine);
                                userPin += lines.size();
                            }
                            if (userPin == pinSku.getRestrictAmount()) {
                                pinActivityDTO.setOrRestrictAmount(1);
                            } else pinActivityDTO.setOrRestrictAmount(0);
                        }
                    } else {
                        if (pinActivity.getStatus().equals("C") || pinActivity.getStatus().equals("F")) {
                            pinActivityDTO.setStatus("E");
                            result.putPOJO("themeList", Json.toJson(getPushPin()));
                        }
                        pinActivityDTO.setOrJoinActivity(0);
                    }
                } else {
                    if (pinActivity.getStatus().equals("C") || pinActivity.getStatus().equals("F")) {
                        pinActivityDTO.setStatus("E");
                        result.putPOJO("themeList", Json.toJson(getPushPin()));
                    }
                    pinActivityDTO.setOrJoinActivity(0);
                }

                //库存信息
                pinActivityDTO.setInvArea(sku.getInvArea());
                pinActivityDTO.setInvAreaNm(sku.getInvAreaNm());
                pinActivityDTO.setInvCustoms(sku.getInvCustoms());
                pinActivityDTO.setPostalTaxRate(sku.getPostalTaxRate());
                pinActivityDTO.setPostalStandard(POSTAL_STANDARD);
                pinActivityDTO.setSkuId(sku.getId());
                pinActivityDTO.setSkuType("pin");
                pinActivityDTO.setSkuTypeId(pinSku.getPinId());
                pinActivityDTO.setPinTieredPriceId(pinActivity.getPinTieredId());

                result.putPOJO("activity", Json.toJson(pinActivityDTO));
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
                return ok(result);
            } else {
                result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.PIN_ACTIVITY_NOT_EXISTS.getIndex()), Message.ErrorCode.PIN_ACTIVITY_NOT_EXISTS.getIndex())));
                return ok(result);
            }
        } catch (Exception ex) {
            Logger.error(ex.getMessage());
            ex.printStackTrace();
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.ERROR.getIndex()), Message.ErrorCode.ERROR.getIndex())));
            return ok(result);
        }
    }

    /**
     * 拼购活动列表
     *
     * @return result
     */
    @Security.Authenticated(UserAuth.class)
    public Result activityList() {

        ObjectNode result = newObject();

        Long userId = (Long) ctx().args.get("userId");

        PinUser pinUser = new PinUser();
        pinUser.setUserId(userId);
        try {
            List<PinUser> pinUserList = promotionService.selectPinUser(pinUser);

            List<PinActivityListDTO> pinActivityListDTOs = new ArrayList<>();

            for (PinUser pin : pinUserList) {

                PinActivityListDTO pinActivityDTO = new PinActivityListDTO();
                PinActivity pinActivity = promotionService.selectPinActivityById(pin.getPinActiveId());

                if (pinActivity.getEndAt().before(new Timestamp(new Date().getTime())) && pinActivity.getStatus().equals("Y") && pinActivity.getJoinPersons() < pinActivity.getPersonNum()) {
                    pinActivity.setStatus("F");
                    system.actorSelection(ACTOR_PIN_FAIL).tell(pinActivity.getPinActiveId(), ActorRef.noSender());
                }

                if (pinActivity.getStatus().equals("C")) {

                    Order order = new Order();
                    order.setUserId(userId);
                    order.setPinActiveId(pin.getPinActiveId());
                    List<Order> orders = cartService.getOrder(order);
                    if (orders.size() > 0) {
                        order = orders.get(0);
                        pinActivityDTO.setOrderId(order.getOrderId());
                    }
                }

                BeanUtils.copyProperties(pinActivityDTO, pinActivity);

                pinActivityDTO.setPinUrl(PROMOTION_URL + "/promotion/pin/activity/" + pinActivity.getPinActiveId());

                pinActivityDTO.setEndCountDown(CalCountDown.getEndTimeSubtract(pinActivityDTO.getEndAt()));

                PinSku pinSku = promotionService.getPinSkuById(pinActivityDTO.getPinId());

                Sku sku = new Sku();
                sku.setId(pinSku.getInvId());
                sku = skuService.getInv(sku);
                pinActivityDTO.setPinSkuUrl(DEPLOY_URL + "/comm/pin/detail/" + sku.getItemId() + "/" + pinSku.getPinId());

                pinActivityDTO.setPinTitle(pinSku.getPinTitle());

                if (pin.isOrMaster())
                    pinActivityDTO.setOrMaster(1);
                else pinActivityDTO.setOrMaster(0);

                JsonNode js_invImg = Json.parse(pinSku.getPinImg());
                if (js_invImg.has("url")) {
                    ((ObjectNode) js_invImg).put("url", IMAGE_URL + js_invImg.get("url").asText());
                }
                pinActivityDTO.setPinImg(js_invImg.toString());

                pinActivityListDTOs.add(pinActivityDTO);

            }

            result.putPOJO("activityList", Json.toJson(pinActivityListDTOs));
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
            return ok(result);

        } catch (Exception e) {
            Logger.error(e.getMessage());
            e.printStackTrace();
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.ERROR.getIndex()), Message.ErrorCode.ERROR.getIndex())));
            return ok(result);
        }
    }

    /**
     * 获取推荐拼购商品
     *
     * @return list
     * @throws Exception
     */
    private List<ThemeItem> getPushPin() throws Exception {
        List<ThemeItem> themeItems = new ArrayList<>();

        List<PinSku> pinSkus = promotionService.getPinSkuStatus(new PinSku());

        for (int i = 0; i < (pinSkus.size() > 3 ? 3 : pinSkus.size()); i++) {

            ThemeItem themeItem = new ThemeItem();
            PinSku pin = pinSkus.get(i);

            Sku inv = new Sku();
            inv.setId(pin.getInvId());
            inv = skuService.getInv(inv);

            themeItem.setCollectCount(inv.getCollectCount());
            themeItem.setItemDiscount(pin.getPinDiscount());
            JsonNode jsonNodeInvImg = Json.parse(pin.getPinImg());
            if (jsonNodeInvImg.has("url")) {
                ((ObjectNode) jsonNodeInvImg).put("url", IMAGE_URL + jsonNodeInvImg.get("url").asText());
                themeItem.setItemImg(Json.stringify(jsonNodeInvImg));
            }
            themeItem.setItemPrice(Json.parse(pin.getFloorPrice()).get("price").decimalValue());
            themeItem.setItemSoldAmount(inv.getSoldAmount());
            themeItem.setItemSrcPrice(inv.getItemSrcPrice());
            themeItem.setItemTitle(inv.getInvTitle());
            themeItem.setItemUrl(DEPLOY_URL + "/comm/pin/detail/" + inv.getItemId() + "/" + pin.getPinId());
            themeItem.setItemType("pin");
            themeItem.setState(pin.getStatus());//商品状态
            themeItem.setStartAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(pin.getStartAt()));
            themeItem.setEndAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(pin.getEndAt()));
            themeItems.add(themeItem);
        }
        return themeItems;
    }
}
