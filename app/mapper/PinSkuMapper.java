package mapper;

import domain.PinActivity;
import domain.PinSku;
import domain.PinTieredPrice;
import domain.PinUser;

import java.util.List;

/**
 *
 * Created by tiffany on 16/1/20.
 */
public interface PinSkuMapper {

    /**
     * 通过ID获取拼购    Added by Tiffany Zhu 2016.01.22
     *
     * @param pinId pinId
     * @return PinSku
     */
    PinSku getPinSkuById(Long pinId);

    PinActivity selectPinActivityById(Long pinActivityId);

    List<PinUser> selectPinUser(PinUser pinUser);

    List<PinSku> getPinSkuStatus(PinSku pinSku);
}
