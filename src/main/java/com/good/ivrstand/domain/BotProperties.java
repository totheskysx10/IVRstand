package com.good.ivrstand.domain;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "telegram.bot")
@Data
public class BotProperties {

    String username;

    String token;

}