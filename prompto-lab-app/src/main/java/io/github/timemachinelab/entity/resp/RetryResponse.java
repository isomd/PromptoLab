package io.github.timemachinelab.entity.resp;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 重试响应结果
 * 
 * @author suifeng
 * @date 2025/1/20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RetryResponse {
    
    /**
     * 节点ID
     */
    private String nodeId;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 重试原因
     */
    private String whyretry;
    
    /**
     * 处理时间戳
     */
    private Long processTime;
}