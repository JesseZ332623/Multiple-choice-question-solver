package com.jesse.examination.controller.viewcontroller;

import com.jesse.examination.entity.scorerecord.ScoreRecordEntity;
import com.jesse.examination.errorhandle.ControllerErrorMessage;
import com.jesse.examination.errorhandle.ErrorMessageGenerator;
import com.jesse.examination.service.scorerecordservice.ScoreRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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

    /*
     * GET 方法请求，渲染所有练习记录，以视图作为响应，
     * 渲染在 AllPractiseScore.html 页面中。
     * 如果期间出现错误，会跳转到统一的 Error_Page.html 页面并显示错误消息。
     *
     * 可能的 URL 为：http://localhost:8081/all_score_record
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

    /*
     * GET 方法请求，在用户交卷后渲染它此时的成绩，以视图作为响应，
     * 渲染在 ScoreSettlement.html 页面中。
     * 如果期间出现错误，会跳转到统一的 Error_Page.html 页面并显示错误消息。
     *
     * 可能的 URL 为：http://localhost:8081/current_score_settlement
     *
     * 需要注意的是，这是一个敏感操作，
     * 用户不得通过这个 URL 直接访问（会以 403 作为响应码拒绝）。
     *
     * 2025-4-10 修改：该方法直接以视图作为相应不合适，
     *                 目前已经改成 restful 控制器的形式了。
     */
    @GetMapping(path = "current_score_settlement")
    public String scoreSettlementView() { return "ScoreSettlement"; }
}
