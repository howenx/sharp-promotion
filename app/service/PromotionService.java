package service;

import domain.PinActivity;
import domain.PinSku;
import domain.PinUser;

import java.util.List;

/**
 * For homepage theme list display function.
 * Created by howen on 15/10/26.
 */
public interface PromotionService {

    PinSku getPinSkuById(Long pinId);


    PinActivity selectPinActivityById(Long pinActivityId);


    List<PinUser> selectPinUser(PinUser pinUser);


    List<PinSku> getPinSkuStatus(PinSku pinSku);
}
