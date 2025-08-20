package io.github.timemachinelab.controller;

import io.github.timemachinelab.entity.req.RetryRequest;
import io.github.timemachinelab.entity.resp.ApiResult;
import io.github.timemachinelab.entity.resp.RetryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 用户交互控制器
 * 提供用户交互相关的API接口
 * 
 * @author suifeng
 * @date 2025/1/20
 */
@Slf4j
@RestController
@RequestMapping("/api/user-interaction")
@Validated
public class UserInteractionController {

    /**
     * 重试接口
     * 
     * @param request 重试请求参数
     * @return 重试结果
     */
    @PostMapping("/retry")
    public ResponseEntity<ApiResult<RetryResponse>> retry(@Valid @RequestBody RetryRequest request) {
        try {
            log.info("收到重试请求 - nodeId: {}, sessionId: {}, whyretry: {}", 
                    request.getNodeId(), request.getSessionId(), request.getWhyretry());
            
            // 构建响应数据
            RetryResponse response = RetryResponse.builder()
                    .nodeId(request.getNodeId())
                    .sessionId(request.getSessionId())
                    .whyretry(request.getWhyretry())
                    .processTime(System.currentTimeMillis())
                    .build();
            
            log.info("重试请求处理成功 - nodeId: {}, sessionId: {}", 
                    request.getNodeId(), request.getSessionId());
            
            return ResponseEntity.ok(ApiResult.success("重试请求处理成功", response));
            
        } catch (Exception e) {
            log.error("重试请求处理失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResult.serverError("重试请求处理失败: " + e.getMessage()));
        }
    }
}