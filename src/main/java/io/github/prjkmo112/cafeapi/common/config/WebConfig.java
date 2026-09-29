package io.github.prjkmo112.cafeapi.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // yyyy-MM-dd HH:mm:ss
    // yyyy-MM-dd HH:mm:ss.SSS
    private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss")
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
            .optionalEnd()
            .toFormatter();

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(
                String.class,
                LocalDateTime.class,
                source -> {
                    if (source.isBlank()) {
                        return null;
                    }

                    return LocalDateTime.parse(source.trim(), DATE_TIME_FORMATTER);
                }
        );
    }
}
