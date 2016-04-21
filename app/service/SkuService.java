package service;

import domain.Sku;
import domain.SysParameter;

/**
 * sku service
 * Created by howen on 15/11/24.
 */
public interface SkuService {

    Sku getInv(Sku sku) throws Exception;

    SysParameter getSysParameter(SysParameter sysParameter);

}
