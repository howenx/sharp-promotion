package service;

import mapper.SkuMapper;
import mapper.SubjectPriceMapper;
import mapper.VaryPriceMapper;
import play.Logger;
import domain.*;
import javax.inject.Inject;
import java.util.List;

/**
 * impl
 * Created by howen on 15/11/24.
 */
public class SkuServiceImpl implements SkuService{
    @Inject
    private SkuMapper skuMapper;

    @Inject
    private VaryPriceMapper varyPriceMapper;

    @Inject
    private SubjectPriceMapper subjectPriceMapper;

    @Override
    public Sku getInv(Sku sku) throws Exception{
        return skuMapper.getInv(sku);
    }

    @Override
    public Boolean updateInv(Sku sku) throws Exception{
        return skuMapper.updateInv(sku)>=0;
    }

    @Override
    public Carriage getCarriage(Carriage carriage) throws Exception {
        return skuMapper.getCarriage(carriage);
    }

    @Override
    public SysParameter getSysParameter(SysParameter sysParameter){
        try{
            return skuMapper.getSysParameter(sysParameter);
        }catch (Exception ex){
            Logger.error(ex.getMessage());
            return null;
        }
    }

    @Override
    public List<VaryPrice> getVaryPriceBy(VaryPrice varyPrice) {
        return varyPriceMapper.getVaryPriceBy(varyPrice);
    }

    @Override
    public SubjectPrice getSbjPriceById(Long id) {
        return subjectPriceMapper.getSbjPriceById(id);
    }

}