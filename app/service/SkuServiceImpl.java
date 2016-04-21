package service;

import domain.Sku;
import domain.SysParameter;
import mapper.SkuMapper;
import play.Logger;

import javax.inject.Inject;

/**
 * impl
 * Created by howen on 15/11/24.
 */
public class SkuServiceImpl implements SkuService {
    @Inject
    private SkuMapper skuMapper;

    @Override
    public Sku getInv(Sku sku) throws Exception {
        return skuMapper.getInv(sku);
    }

    @Override
    public SysParameter getSysParameter(SysParameter sysParameter) {
        try {
            return skuMapper.getSysParameter(sysParameter);
        } catch (Exception ex) {
            Logger.error(ex.getMessage());
            return null;
        }
    }
}
