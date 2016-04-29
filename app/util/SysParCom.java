package util;

import domain.SysParameter;
import play.Configuration;
import service.SkuService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 查询参数表中的参数项
 * Created by hao on 16/2/28.
 */
@Singleton
public class SysParCom {

    //行邮税收税标准
    public static String POSTAL_STANDARD;

    //海关规定购买单笔订单金额限制
    public static String POSTAL_LIMIT;

    //达到多少免除邮费
    public static String FREE_SHIP;

    //shopping服务器url
    public static String SHOPPING_URL;

    public static String PROMOTION_URL;

    public static String JD_SECRET;

    public static String JD_SELLER;

    public static Long COUNTDOWN_MILLISECONDS;

    public static Long PIN_MILLISECONDS;

    //发布服务器url
    public static String DEPLOY_URL;


    //id服务器url
    public static String ID_URL;

    public static String IMAGE_URL;

    public static String ACTOR_PIN_FAIL;

    public static String REDIS_URL;
    public static String REDIS_PASSWORD;
    public static Integer REDIS_PORT;
    public static String REDIS_CHANNEL;


    @Inject
    public SysParCom(SkuService skuService, Configuration configuration) {

        POSTAL_STANDARD = skuService.getSysParameter(new SysParameter(null, null, null, "POSTAL_STANDARD")).getParameterVal();

        POSTAL_LIMIT = skuService.getSysParameter(new SysParameter(null, null, null, "POSTAL_LIMIT")).getParameterVal();

        FREE_SHIP = skuService.getSysParameter(new SysParameter(null, null, null, "FREE_SHIP")).getParameterVal();

        SHOPPING_URL = configuration.getString("shopping.server.url");

        PROMOTION_URL = configuration.getString("promotion.server.url");

        JD_SECRET = configuration.getString("jd_secret");

        JD_SELLER = configuration.getString("jd_seller");

        COUNTDOWN_MILLISECONDS = configuration.getLong("order.countdown.milliseconds");

        PIN_MILLISECONDS = configuration.getLong("pin.activity.milliseconds");

        DEPLOY_URL = configuration.getString("deploy.server.url");

        ID_URL = configuration.getString("id.server.url");

        IMAGE_URL = configuration.getString("image.server.url");

        ACTOR_PIN_FAIL =  configuration.getString("actor.pin.fail");

        REDIS_URL = configuration.getString("redis.host");
        REDIS_PASSWORD = configuration.getString("redis.password");
        REDIS_PORT = configuration.getInt("redis.port");
        REDIS_CHANNEL = configuration.getString("redis.channel");

    }

}
