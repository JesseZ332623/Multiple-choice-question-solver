package com.jesse.examination.errorhandle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 控制器方法错误信息类。
 */
@Data
@Builder
@AllArgsConstructor
public class ControllerErrorMessage
{
    private final String controllerName;
    private final String controllerMethod;
    private final String errorMessage;

    @Override
    public String toString()
    {
        return String.format(
                "%s\n%s\n%s",
                this.getControllerName(),
                this.getControllerMethod(),
                this.getErrorMessage()
        );
    }
}
