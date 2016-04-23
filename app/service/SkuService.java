package service;

import domain.Sku;
import domain.SysParameter;
import domain.VersionVo;

import java.util.List;

/**
 * sku service
 * Created by howen on 15/11/24.
 */
public interface SkuService {

    Sku getInv(Sku sku) throws Exception;

    SysParameter getSysParameter(SysParameter sysParameter);

    List<VersionVo> getVersioning(VersionVo versionVo);

}
