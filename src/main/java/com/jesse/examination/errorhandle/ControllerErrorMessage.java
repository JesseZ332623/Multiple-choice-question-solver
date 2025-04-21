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
    private final String controllerName;            // 控制器名（通过反射获取）
    private final String controllerMethod;          // 方法名  （当前是手动传入）
    private final String errorMessage;              // 错误信息

    /**
     * 返回错误描述的详细字符串。
     */
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
