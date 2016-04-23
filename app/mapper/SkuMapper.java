package mapper;

import domain.Sku;
import domain.SysParameter;
import domain.VersionVo;

import java.util.List;

/**
 * 商品库查询,更新
 * Created by howen on 15/11/22.
 */
public interface SkuMapper {

    Sku getInv(Sku sku) throws Exception;

    SysParameter getSysParameter(SysParameter sysParameter) throws Exception;

    List<VersionVo> getVersioning(VersionVo versionVo);

}
