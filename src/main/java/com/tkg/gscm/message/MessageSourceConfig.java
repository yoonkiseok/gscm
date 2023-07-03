package com.tkg.gscm.message;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;

@Configuration
public class MessageSourceConfig {

    @Value("${app.locale}") // 환경 변수에서 로케일 정보를 가져옵니다
    private String appLocale;

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();

        // 업무별 프로퍼티 파일을 설정합니다
        String[] basenames = {
                "messages",
                "messages/work1/messages_work1",
                "messages/work2/messages_work2"
        };

        messageSource.setBasenames(basenames);
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);

        // 환경 변수로부터 가져온 로케일을 설정합니다
        messageSource.setDefaultLocale(new Locale(appLocale));

        return messageSource;
    }
}