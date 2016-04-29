package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import play.Logger;
import play.libs.Json;
import redis.clients.jedis.Jedis;

import javax.inject.Inject;

import static util.SysParCom.REDIS_CHANNEL;

/**
 * 用于produce消息到阿里云mns的Actor
 * Created by howen on 15/12/24.
 */
public class MnsActor extends AbstractActor {

    @Inject
    public MnsActor(Jedis jedis) {
        receive(ReceiveBuilder.match(Object.class, event -> {

            if (event instanceof ILoggingEvent) {
                ((ILoggingEvent) event).getMDCPropertyMap().put("projectId", "style-promotion");
//                System.out.println("日志Json格式---->" + Json.toJson(event).toString());
                jedis.publish(REDIS_CHANNEL,Json.toJson(event).toString());
            }
        }).matchAny(s -> {
            Logger.error("MnsActor received messages not matched: {}", s.toString());
            unhandled(s);
        }).build());
    }
}