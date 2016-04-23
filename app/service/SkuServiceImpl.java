package service;

import domain.Sku;
import domain.SysParameter;
import domain.VersionVo;
import mapper.SkuMapper;
import play.Logger;

import javax.inject.Inject;
import java.util.List;

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

    @Override
    public List<VersionVo> getVersioning(VersionVo versionVo) {
        return skuMapper.getVersioning(versionVo);
    }
}
