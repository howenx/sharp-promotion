package filters;

import com.fasterxml.jackson.databind.JsonNode;
import net.spy.memcached.MemcachedClient;
import play.Logger;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Security;

import javax.inject.Inject;
import java.util.Optional;

/**
 * 用户校验
 * Created by howen on 15/11/25.
 */
public class UserAuth extends Security.Authenticator {

    @Inject
    private MemcachedClient cache;

    @Override
    public String getUsername(Http.Context ctx) {
        Optional<String> header = Optional.ofNullable(ctx.request().getHeader("id-token"));
        if (header.isPresent()) {
            Optional<Object> token = Optional.ofNullable(cache.get(header.get()));
            if (token.isPresent()) {
                JsonNode userJson = Json.parse(token.get().toString());
//                Logger.info("Cache中的用户信息:"+userJson.toString());
                Long userId = Long.valueOf(userJson.findValue("id").asText());
                String  username = userJson.findValue("name").toString();
                ctx.args.put("userId",userId);
                ctx.args.put("username",username);
                ctx.args.put("userPhoto",userJson.findValue("photo").toString().trim());
                return username;
            }
            else return null;
        }else return null;
    }
}

