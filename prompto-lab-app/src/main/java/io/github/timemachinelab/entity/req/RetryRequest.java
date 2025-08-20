package io.github.timemachinelab.entity.req;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 重试请求参数
 * 
 * @author suifeng
 * @date 2025/1/20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetryRequest {
    
    /**
     * 节点ID
     */
    @NotNull(message = "nodeId不能为null")
    @NotBlank(message = "nodeId不能为空")
    private String nodeId;
    
    /**
     * 会话ID
     */
    @NotNull(message = "sessionId不能为null")
    @NotBlank(message = "sessionId不能为空")
    private String sessionId;
    
    /**
     * 重试原因
     */
    @NotNull(message = "whyretry不能为null")
    @NotBlank(message = "whyretry不能为空")
    private String whyretry;
}