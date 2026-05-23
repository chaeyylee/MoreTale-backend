package com.moretale.domain.story.controller;

import com.moretale.global.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/internal/ai/story/callbacks")
public class StoryGenerationCallbackController {

    @PostMapping
    public ApiResponse<Void> handleStoryCallback() {
        log.info("AI story callback accepted");
        return ApiResponse.success(null, "AI callback accepted");
    }
}
