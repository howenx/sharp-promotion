package service;

import domain.PinActivity;
import domain.PinSku;
import domain.PinUser;
import mapper.PinSkuMapper;

import javax.inject.Inject;
import java.util.List;

/**
 * PromotionServiceImpl
 * Created by howen on 16/1/25.
 */
public class PromotionServiceImpl implements PromotionService {

    @Inject
    private PinSkuMapper pinSkuMapper;

    @Override
    public PinSku getPinSkuById(Long pinId) {
        return pinSkuMapper.getPinSkuById(pinId);
    }

    @Override
    public PinActivity selectPinActivityById(Long pinActivityId) {
        return pinSkuMapper.selectPinActivityById(pinActivityId);
    }

    @Override
    public List<PinUser> selectPinUser(PinUser pinUser) {
        return pinSkuMapper.selectPinUser(pinUser);
    }


    @Override
    public List<PinSku> getPinSkuStatus(PinSku pinSku) {
        return pinSkuMapper.getPinSkuStatus(pinSku);
    }
}
