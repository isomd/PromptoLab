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
 * 描述: 模型配置实体类
 * 用于存储AI模型的配置信息
 * 
 * @author suifeng
 * 日期: 2025/1/27
 */
@Entity
@Table(name = "ai_model_configs")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelConfigEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "model_name", length = 100, nullable = false, unique = true)
    private String modelName;
    
    @Column(name = "provider", length = 50, nullable = false)
    private String provider;
    
    @Column(name = "api_key", length = 500)
    private String apiKey;
    
    @Column(name = "base_url", length = 500)
    private String baseUrl;
    
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Type(type = "jsonb")
    @Column(name = "custom_params", columnDefinition = "jsonb")
    private Map<String, Object> customParams;
    
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