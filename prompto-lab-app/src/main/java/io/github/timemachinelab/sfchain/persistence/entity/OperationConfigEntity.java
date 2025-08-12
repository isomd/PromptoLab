package io.github.timemachinelab.sfchain.persistence.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 描述: 操作配置实体类
 * 用于存储AI操作的配置信息
 * 
 * @author suifeng
 * 日期: 2025/1/27
 */
@Entity
@Table(name = "ai_operation_configs")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationConfigEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "operation_type", length = 100, nullable = false, unique = true)
    private String operationType;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
    
    @Column(name = "max_tokens")
    private Integer maxTokens;
    
    @Column(name = "temperature")
    private Double temperature;
    
    @Column(name = "json_output", nullable = false)
    private Boolean jsonOutput = false;
    
    @Column(name = "thinking_mode", nullable = false)
    private Boolean thinkingMode = false;
    
    @Type(type = "jsonb")
    @Column(name = "custom_params", columnDefinition = "jsonb")
    private Map<String, Object> customParams;
    
    /**
     * 关联的模型名称
     */
    @Column(name = "model_name", length = 100)
    private String modelName;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}