package com.jesse.examination.errorhandle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Contract;

/**
 * 错误信息生成工具类。
 */
public class ErrorMessageGenerator
{
    /**
     * 针对控制器方法产生的错误信息，最终渲染在错误页面。
     *
     * @param controllerName 控制器名
     * @param methodName     控制器方法名
     * @param exceptionMsg   异常信息
     *
     * @return 页面上返回的错误信息可能的格式是：
     *            Controller Name: QuestionPractiseViewController
     *            Controller Method: getQuestionPractise()
     *            Error: [IllegalStateException] Query Operator Failed.....
     */
    @NotNull
    @Contract(pure = true)
    static public ControllerErrorMessage getErrorMessage(
            String controllerName, String methodName, String exceptionMsg
    )
    {
        return new ControllerErrorMessage.ControllerErrorMessageBuilder()
                   .controllerName("Controller Name: " + controllerName)
                   .controllerMethod(String.format("Controller Method: %s.%s()", controllerName, methodName))
                   .errorMessage(exceptionMsg).build();
    }
}
