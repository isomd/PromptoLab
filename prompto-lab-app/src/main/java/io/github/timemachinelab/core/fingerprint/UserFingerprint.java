package io.github.timemachinelab.core.fingerprint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户指纹实体
 * 包含用户指纹及相关访问信息
 * 
 * @author suifeng
 * @date 2025/1/27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFingerprint {
    
    /**
     * 用户指纹（16位哈希值）
     */
    private String fingerprint;
    
    /**
     * 首次访问时间戳
     */
    private Long firstSeen;
    
    /**
     * 最后访问时间戳
     */
    private Long lastSeen;
    
    /**
     * 访问次数
     */
    private Integer visitCount;
    
    /**
     * 最后访问的IP地址
     */
    private String lastIp;
    
    /**
     * 用户代理字符串
     */
    private String userAgent;
    
    /**
     * 用户昵称（可选，用户可以设置）
     */
    private String nickname;
    
    /**
     * 用户偏好设置（JSON字符串）
     */
    private String preferences;
    
    /**
     * 获取用户显示名称
     * 优先使用昵称，否则使用指纹的前8位
     * 
     * @return 用户显示名称
     */
    public String getDisplayName() {
        if (nickname != null && !nickname.trim().isEmpty()) {
            return nickname;
        }
        return "用户_" + (fingerprint != null ? fingerprint.substring(0, 8) : "unknown");
    }
}