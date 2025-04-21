package com.jesse.examination.scorerecord.controller;

import com.jesse.examination.scorerecord.entity.ScoreRecordEntity;
import com.jesse.examination.errorhandle.ControllerErrorMessage;
import com.jesse.examination.errorhandle.ErrorMessageGenerator;
import com.jesse.examination.scorerecord.service.ScoreRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequestMapping(path = "/")
public class PractiseScoreViewController
{
    private final ScoreRecordService scoreRecordService;

    @Autowired
    public PractiseScoreViewController(ScoreRecordService scoreRecordService) {
        this.scoreRecordService = scoreRecordService;
    }

    /**
     * GET 方法请求，渲染所有练习记录，以视图作为响应，
     * 渲染在 AllPractiseScore.html 页面中。
     * 如果期间出现错误，会跳转到统一的 Error_Page.html 页面并显示错误消息。
     *
     * <p>
     *      链接：
     *      <a href="https://localhost:8081/all_score_record">
     *          (GET Method) 渲染所有练习记录，以视图作为响应。
     *     </a>
     * </p>
     */
    @GetMapping(path = "all_score_record")
    public String getAllPractiseScoreView(Model model)
    {
        try
        {
            List<ScoreRecordEntity> allPractiseScoreQueryRes
                    = this.scoreRecordService.findAllScoreRecord();

            model.addAttribute("AllScoreRecord", allPractiseScoreQueryRes);

            return "AllPractiseScore";
        }
        catch (Exception exception)
        {
            ControllerErrorMessage errorMessage
                    = ErrorMessageGenerator.getErrorMessage(
                            this.getClass().getSimpleName(),
                    "getAllPractiseScoreView",
                    exception.getMessage()
            );

            log.error(errorMessage.toString());

            model.addAttribute("ErrorMessage", errorMessage);

            return "Error_Page";
        }
    }

    /**
     * GET 方法请求，在用户交卷后渲染它此时的成绩，以视图作为响应，
     * 渲染在 ScoreSettlement.html 页面中。
     * 如果期间出现错误，会跳转到统一的 Error_Page.html 页面并显示错误消息。
     *
     * <p>
     *      链接：
     *      <a href="https://localhost:8081/current_score_settlement">
     *          (GET Method) 渲染所有练习记录，以视图作为响应。
     *     </a>
     * </p>
     *
     * <strong>
     *     若用户直接访问的话，就会以最新的一次练习记录作为数据进行渲染。
     * </strong>
     */
    @GetMapping(path = "current_score_settlement")
    public String scoreSettlementView() { return "ScoreSettlement"; }
}
