package com.moretale.domain.story.service;

import com.moretale.domain.story.dto.StoryGenerateRequest;
import com.moretale.domain.story.dto.StoryGenerateResponse;
import com.moretale.domain.story.dto.StoryGenerationJobResponse;

public interface AIStoryService {

    StoryGenerationJobResponse enqueueStoryJob(
            StoryGenerateRequest request,
            String callbackUrl,
            String requestId
    );

    StoryGenerationJobResponse getStoryJobStatus(String jobId);

    StoryGenerateResponse getStoryJobResult(String jobId);
}
