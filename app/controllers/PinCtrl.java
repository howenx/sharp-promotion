package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import domain.*;
import filters.UserAuth;
import net.spy.memcached.MemcachedClient;
import org.apache.commons.beanutils.BeanUtils;
import play.Logger;
import play.Play;
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
import java.util.*;
import java.util.stream.Collectors;

import static play.libs.Json.newObject;

/**
 * 拼购
 * Created by howen on 16/2/17.
 */
public class PinCtrl extends Controller {

    private SkuService skuService;

    private CartService cartService;

    private IdService idService;

    private PromotionService promotionService;

    public static final String PIN_USER_PHOTO = Play.application().configuration().getString("oss.url");

    //图片服务器url
    public static final String IMAGE_URL = play.Play.application().configuration().getString("image.server.url");

    //发布服务器url
    public static final String DEPLOY_URL = play.Play.application().configuration().getString("deploy.server.url");

    //shopping服务器url
    public static final String SHOPPING_URL = play.Play.application().configuration().getString("shopping.server.url");

    //id服务器url
    public static final String ID_URL = play.Play.application().configuration().getString("id.server.url");

    //promotion服务器url
    public static final String PROMOTION_URL = play.Play.application().configuration().getString("promotion.server.url");

    public static final String IMG_PROCESS_URL = play.Play.application().configuration().getString("imgprocess.server.url");

    //将Json串转换成List
    private static ObjectMapper mapper = new ObjectMapper();

    @Inject
    private MemcachedClient cache;


    @Inject
    public PinCtrl(SkuService skuService, CartService cartService, IdService idService, PromotionService promotionService) {
        this.cartService = cartService;
        this.idService = idService;
        this.skuService = skuService;
        this.promotionService = promotionService;

    }

    public Result testpin() {
        Map<String, String> params = new HashMap<>();
        params.put("pinActivity", PROMOTION_URL + "/promotion/pin/activity/pay/" + 223667);
        return ok(views.html.pin.render(params));
    }

    /**
     * 拼购活动页
     *
     * @param activityId 活动ID
     * @param pay        是否开团页
     * @return json
     */
    public Result pinActivity(Long activityId, Integer pay,Integer userPayType) {

        ObjectNode result = newObject();

        Optional<String> header = Optional.ofNullable(request().getHeader("id-token"));

        try {

            PinActivityDTO pinActivityDTO = new PinActivityDTO();

            PinActivity pinActivity = promotionService.selectPinActivityById(activityId);

            BeanUtils.copyProperties(pinActivityDTO, pinActivity);

            PinUser pinUser = new PinUser();
            pinUser.setPinActiveId(pinActivity.getPinActiveId());
            List<PinUser> pinUserList = promotionService.selectPinUser(pinUser);

            pinUserList = pinUserList.stream().map(p -> {
                p.setUserImg(PIN_USER_PHOTO + p.getUserImg());
                try {
                    ID userNm = idService.getID(p.getUserId());
                    if (userNm == null)
                        p.setUserNm(("HMM-" + GenCouponCode.GetCode(4)).toLowerCase());
                    else p.setUserNm(idService.getID(p.getUserId()).getNickname());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return p;
            }).collect(Collectors.toList());

            pinActivityDTO.setPinUsers(pinUserList);

            if (pay == 2) {
                pinActivityDTO.setPay("new");
                if (userPayType==1) pinActivityDTO.setUserType("master");
                else pinActivityDTO.setUserType("ordinary");
            }
            else pinActivityDTO.setPay("normal");

            pinActivityDTO.setPinUrl(PROMOTION_URL + "/promotion/pin/activity/" + activityId);

            pinActivityDTO.setEndCountDown(CalCountDown.getEndTimeSubtract(pinActivityDTO.getEndAt()));

            PinSku pinSku = promotionService.getPinSkuById(pinActivityDTO.getPinId());


            Sku sku = new Sku();
            sku.setId(pinSku.getInvId());
            sku = skuService.getInv(sku);
            pinActivityDTO.setPinSkuUrl(DEPLOY_URL + "/comm/pin/detail/" + sku.getItemId() + "/" + sku.getId() + "/" + pinSku.getPinId());

            pinActivityDTO.setPinTitle(pinSku.getPinTitle());


            JsonNode js_invImg = Json.parse(pinSku.getPinImg());
            if (js_invImg.has("url")) {
                ((ObjectNode) js_invImg).put("url", IMAGE_URL + js_invImg.get("url").asText());
            }
            pinActivityDTO.setPinImg(js_invImg.toString());


            //用户状态
            if (header.isPresent()) {
                Optional<String> token = Optional.ofNullable(cache.get(header.get()).toString());
                if (token.isPresent()) {
                    JsonNode userJson = Json.parse(token.get());
                    Long userId = Long.valueOf(userJson.findValue("id").asText());
                    PinUser pu = new PinUser();
                    pu.setUserId(userId);
                    pu.setPinActiveId(activityId);
                    List<PinUser> pinUsers = promotionService.selectPinUser(pu);
                    if (pinUsers.size()>0){
                        pinActivityDTO.setOrJoinActivity(1);
                    }else pinActivityDTO.setOrJoinActivity(0);
                }else pinActivityDTO.setOrJoinActivity(0);
            }else pinActivityDTO.setOrJoinActivity(0);

            result.putPOJO("activity", Json.toJson(pinActivityDTO));
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.SUCCESS.getIndex()), Message.ErrorCode.SUCCESS.getIndex())));
            return ok(result);
        } catch (Exception ex) {
            Logger.error(ex.getMessage());
            ex.printStackTrace();
            result.putPOJO("message", Json.toJson(new Message(Message.ErrorCode.getName(Message.ErrorCode.ERROR.getIndex()), Message.ErrorCode.ERROR.getIndex())));
            return ok(result);
        }
    }

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

                BeanUtils.copyProperties(pinActivityDTO, pinActivity);

                pinActivityDTO.setPinUrl(PROMOTION_URL + "/promotion/pin/activity/" + pinActivity.getPinActiveId());

                pinActivityDTO.setEndCountDown(CalCountDown.getEndTimeSubtract(pinActivityDTO.getEndAt()));

                PinSku pinSku = promotionService.getPinSkuById(pinActivityDTO.getPinId());

                Sku sku = new Sku();
                sku.setId(pinSku.getInvId());
                sku = skuService.getInv(sku);
                pinActivityDTO.setPinSkuUrl(DEPLOY_URL + "/comm/pin/detail/" + sku.getItemId() + "/" + sku.getId() + "/" + pinSku.getPinId());

                pinActivityDTO.setPinTitle(pinSku.getPinTitle());

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

}
