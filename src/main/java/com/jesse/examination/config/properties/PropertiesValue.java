package com.jesse.examination.config.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

@Component
@Validated
public class PropertiesValue
{
    @NotNull
    @Value("${file.upload-dir}")
    private String fileUploadPath;

    @Value("${spring.session.timeout}")
    private Duration sessionTimeOut;

    public Path getFileUploadPath()
    {
        return Paths.get(this.fileUploadPath)
                    .toAbsolutePath().normalize();
    }

    public long getSessionTimeOutSeconds() {
        return this.sessionTimeOut.toSeconds();
    }
}
