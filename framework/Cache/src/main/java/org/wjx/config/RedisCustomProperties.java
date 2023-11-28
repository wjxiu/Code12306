package org.wjx.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author xiu
 * @create 2023-11-25 14:43
 */
@Component
@Getter
@Setter
@ConfigurationProperties(prefix = RedisCustomProperties.PREFIX)
public class RedisCustomProperties {
  public static final   String PREFIX ="frame.redis";
  private String prefixCharset = "UTF-8";
  public  Long timeOut =30000L;
  public  TimeUnit timeUnit=TimeUnit.MILLISECONDS;
}
