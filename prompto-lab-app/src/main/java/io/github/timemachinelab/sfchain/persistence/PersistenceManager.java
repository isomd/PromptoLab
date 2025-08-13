package io.github.timemachinelab.sfchain.persistence;

import io.github.timemachinelab.sfchain.core.AIOperationRegistry;
import io.github.timemachinelab.sfchain.core.ModelRegistry;
import io.github.timemachinelab.sfchain.core.openai.OpenAIModelConfig;
import io.github.timemachinelab.sfchain.core.openai.OpenAIModelFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 描述: 持久化管理器
 * 负责协调持久化服务与现有的模型注册和操作注册系统
 * 
 * @author suifeng
 * 日期: 2025/1/27
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PersistenceManager {
    
    private final PersistenceService persistenceService;
    private final ModelRegistry modelRegistry;
    private final AIOperationRegistry operationRegistry;
    private final OpenAIModelFactory modelFactory;
    private final DynamicOperationConfigService dynamicOperationConfigService;
    
    /**
     * 应用启动完成后同步配置
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("开始同步持久化配置...");
        
        // 同步现有配置到持久化存储
        syncExistingConfigurations();
        
        // 从持久化存储加载额外配置
        loadPersistedConfigurations();
        
        log.info("持久化配置同步完成");
    }
    
    // ==================== 模型配置管理 ====================
    
    /**
     * 添加模型配置
     * 
     * @param modelName 模型名称
     * @param config 模型配置数据
     */
    public void addModelConfig(String modelName, ModelConfigData config) {
        try {
            // 验证配置
            if (!config.isValid()) {
                throw new IllegalArgumentException("无效的模型配置: " + modelName);
            }
            
            // 转换为OpenAI模型配置
            OpenAIModelConfig openAIConfig = convertToOpenAIConfig(config);
            
            // 注册到模型工厂
            modelFactory.registerModel(openAIConfig);
            
            // 保存到持久化存储
            persistenceService.saveModelConfig(modelName, config);
            
            log.info("成功添加模型配置: {}", modelName);
        } catch (Exception e) {
            log.error("添加模型配置失败: {} - {}", modelName, e.getMessage());
            throw new RuntimeException("添加模型配置失败: " + modelName, e);
        }
    }
    
    /**
     * 更新模型配置
     * 
     * @param modelName 模型名称
     * @param config 模型配置数据
     */
    public void updateModelConfig(String modelName, ModelConfigData config) {
        try {
            // 检查模型是否存在
            if (!persistenceService.existsModelConfig(modelName)) {
                throw new IllegalArgumentException("模型配置不存在: " + modelName);
            }
            
            // 验证配置
            if (!config.isValid()) {
                throw new IllegalArgumentException("无效的模型配置: " + modelName);
            }
            
            // 移除旧配置
            modelFactory.removeModel(modelName);
            
            // 转换为OpenAI模型配置
            OpenAIModelConfig openAIConfig = convertToOpenAIConfig(config);
            
            // 重新注册到模型工厂
            modelFactory.registerModel(openAIConfig);
            
            // 更新持久化存储
            persistenceService.saveModelConfig(modelName, config);
            
            log.info("成功更新模型配置: {}", modelName);
        } catch (Exception e) {
            log.error("更新模型配置失败: {} - {}", modelName, e.getMessage());
            throw new RuntimeException("更新模型配置失败: " + modelName, e);
        }
    }
    
    /**
     * 删除模型配置
     * 
     * @param modelName 模型名称
     */
    public void deleteModelConfig(String modelName) {
        try {
            // 检查是否有操作正在使用此模型（从操作配置中检查）
            Map<String, OperationConfigData> allConfigs = persistenceService.getAllOperationConfigs();
            boolean inUse = allConfigs.values().stream()
                    .anyMatch(config -> modelName.equals(config.getModelName()));
            if (inUse) {
                throw new IllegalStateException("模型正在被操作使用，无法删除: " + modelName);
            }
            
            // 从模型工厂移除
            modelFactory.removeModel(modelName);
            
            // 从持久化存储删除
            persistenceService.deleteModelConfig(modelName);
            
            log.info("成功删除模型配置: {}", modelName);
        } catch (Exception e) {
            log.error("删除模型配置失败: {} - {}", modelName, e.getMessage());
            throw new RuntimeException("删除模型配置失败: " + modelName, e);
        }
    }
    
    /**
     * 获取模型配置
     * 
     * @param modelName 模型名称
     * @return 模型配置
     */
    public Optional<ModelConfigData> getModelConfig(String modelName) {
        return persistenceService.getModelConfig(modelName);
    }
    
    /**
     * 获取所有模型配置
     * 
     * @return 所有模型配置
     */
    public Map<String, ModelConfigData> getAllModelConfigs() {
        return persistenceService.getAllModelConfigs();
    }
    
    // ==================== 操作配置管理 ====================
    
    /**
     * 保存操作配置
     * 同时更新数据库和内存容器
     * 
     * @param operationType 操作类型
     * @param config 操作配置
     */
    public void saveOperationConfig(String operationType, OperationConfigData config) {
        try {
            // 验证操作是否已注册
            if (!operationRegistry.isOperationRegistered(operationType)) {
                throw new IllegalArgumentException("操作未注册: " + operationType);
            }
            
            // 验证配置
            if (!config.isValid()) {
                throw new IllegalArgumentException("无效的操作配置: " + operationType);
            }
            
            // 如果配置中指定了模型，验证模型是否存在
            if (config.getModelName() != null && !config.getModelName().isEmpty()) {
                if (!persistenceService.existsModelConfig(config.getModelName()) && 
                    !modelRegistry.isModelRegistered(config.getModelName())) {
                    throw new IllegalArgumentException("指定的模型不存在: " + config.getModelName());
                }
            }
            
            // 1. 先保存到数据库
            persistenceService.saveOperationConfig(operationType, config);
            
            // 2. 再同步更新操作注册中心的模型映射
            if (config.getModelName() != null && !config.getModelName().isEmpty()) {
                operationRegistry.setModelForOperation(operationType, config.getModelName());
            } else {
                // 如果模型名称为空，移除映射
                operationRegistry.getModelMapping().remove(operationType);
            }
            
            log.info("成功保存操作配置并同步到内存容器: {} -> 模型: {}", operationType, config.getModelName());
        } catch (Exception e) {
            log.error("保存操作配置失败: {} - {}", operationType, e.getMessage());
            throw new RuntimeException("保存操作配置失败: " + operationType, e);
        }
    }
    
    /**
     * 获取操作配置
     * 优先从数据库获取，如果不存在则从@AIOp注解获取
     * 
     * @param operationType 操作类型
     * @return 操作配置
     */
    public Optional<OperationConfigData> getOperationConfig(String operationType) {
        // 优先从数据库获取
        Optional<OperationConfigData> persistedConfig = persistenceService.getOperationConfig(operationType);
        if (persistedConfig.isPresent()) {
            log.debug("从数据库获取操作配置: {}", operationType);
            return persistedConfig;
        }
        
        // 如果数据库配置不存在，则从注解获取
        Optional<OperationConfigData> dynamicConfig = dynamicOperationConfigService.getOperationConfig(operationType);
        if (dynamicConfig.isPresent()) {
            log.debug("从注解获取操作配置: {}", operationType);
        }
        
        return dynamicConfig;
    }
    
    /**
     * 获取所有操作配置
     * 优先使用数据库配置，注解配置作为补充
     * 
     * @return 所有操作配置
     */
    public Map<String, OperationConfigData> getAllOperationConfigs() {
        // 获取持久化配置（数据库）
        Map<String, OperationConfigData> persistedConfigs = persistenceService.getAllOperationConfigs();
        
        // 获取动态配置（从注解）
        Map<String, OperationConfigData> dynamicConfigs = dynamicOperationConfigService.getAllOperationConfigs();
        
        // 合并配置，数据库配置优先
        Map<String, OperationConfigData> allConfigs = new HashMap<>(dynamicConfigs);
        allConfigs.putAll(persistedConfigs); // 数据库配置覆盖注解配置
        
        log.debug("获取所有操作配置: 持久化配置{}个, 动态配置{}个, 总计{}个", 
                persistedConfigs.size(), dynamicConfigs.size(), allConfigs.size());
        
        return allConfigs;
    }
    
    /**
     * 删除操作配置
     * 
     * @param operationType 操作类型
     */
    public void deleteOperationConfig(String operationType) {
        try {
            persistenceService.deleteOperationConfig(operationType);
            
            // 同时从操作注册中心移除模型映射
            operationRegistry.getModelMapping().remove(operationType);
            
            log.info("成功删除操作配置: {}", operationType);
        } catch (Exception e) {
            log.error("删除操作配置失败: {} - {}", operationType, e.getMessage());
            throw new RuntimeException("删除操作配置失败: " + operationType, e);
        }
    }
    
    // ==================== 备份和恢复 ====================
    
    /**
     * 创建配置备份
     * 
     * @param backupName 备份名称
     */
    public void createBackup(String backupName) {
        persistenceService.backup(backupName);
    }
    
    /**
     * 从备份恢复配置
     * 
     * @param backupName 备份名称
     */
    public void restoreFromBackup(String backupName) {
        try {
            // 恢复配置
            persistenceService.restoreFromBackup(backupName);
            
            // 重新同步配置
            loadPersistedConfigurations();
            
            log.info("成功从备份恢复配置: {}", backupName);
        } catch (Exception e) {
            log.error("从备份恢复配置失败: {} - {}", backupName, e.getMessage());
            throw new RuntimeException("从备份恢复配置失败: " + backupName, e);
        }
    }
    
    /**
     * 获取所有备份名称
     * 
     * @return 备份名称列表
     */
    public java.util.List<String> getAllBackupNames() {
        return persistenceService.getAllBackupNames();
    }
    
    /**
     * 刷新配置到持久化存储
     */
    public void flushConfigurations() {
        syncExistingConfigurations();
        log.info("配置已刷新到持久化存储");
    }
    
    /**
     * 重新加载配置
     */
    public void reloadConfigurations() {
        try {
            // 重新加载配置
            loadPersistedConfigurations();
            
            log.info("配置重新加载完成");
        } catch (Exception e) {
            log.error("重新加载配置失败: {}", e.getMessage(), e);
            throw new RuntimeException("重新加载配置失败", e);
        }
    }
    
    // ==================== 私有方法 ====================
    
    /**
     * 同步现有配置到持久化存储
     */
    private void syncExistingConfigurations() {
        try {
            // 同步现有的模型配置
            for (String modelName : modelFactory.getRegisteredModelNames()) {
                OpenAIModelConfig openAIConfig = modelFactory.getModelConfig(modelName);
                if (openAIConfig != null && !persistenceService.existsModelConfig(modelName)) {
                    ModelConfigData configData = convertFromOpenAIConfig(openAIConfig);
                    persistenceService.saveModelConfig(modelName, configData);
                }
            }
            
            // 初始化操作配置：从@AIOp注解获取配置并保存到数据库
            initializeOperationConfigs();
            
        } catch (Exception e) {
            log.warn("同步现有配置时出现警告: {}", e.getMessage());
        }
    }
    
    /**
     * 从持久化存储加载配置
     */
    private void loadPersistedConfigurations() {
        try {
            // 加载模型配置
            Map<String, ModelConfigData> modelConfigs = persistenceService.getAllModelConfigs();
            for (Map.Entry<String, ModelConfigData> entry : modelConfigs.entrySet()) {
                String modelName = entry.getKey();
                ModelConfigData config = entry.getValue();
                
                if (!modelFactory.isModelRegistered(modelName)) {
                    try {
                        OpenAIModelConfig openAIConfig = convertToOpenAIConfig(config);
                        modelFactory.registerModel(openAIConfig);
                        log.info("从持久化存储加载模型配置: {}", modelName);
                    } catch (Exception e) {
                        log.warn("加载模型配置失败: {} - {}", modelName, e.getMessage());
                    }
                }
            }
            
            // 加载操作配置并同步模型映射到操作注册中心
            Map<String, OperationConfigData> operationConfigs = persistenceService.getAllOperationConfigs();
            for (Map.Entry<String, OperationConfigData> entry : operationConfigs.entrySet()) {
                String operationType = entry.getKey();
                OperationConfigData config = entry.getValue();
                
                // 如果操作配置中指定了模型，同步到操作注册中心
                if (config.getModelName() != null && !config.getModelName().isEmpty() && 
                    operationRegistry.isOperationRegistered(operationType)) {
                    try {
                        operationRegistry.setModelForOperation(operationType, config.getModelName());
                        log.debug("从持久化存储同步操作模型映射: {} -> {}", operationType, config.getModelName());
                    } catch (Exception e) {
                        log.warn("同步操作模型映射失败: {} -> {} - {}", operationType, config.getModelName(), e.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("加载持久化配置失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 初始化操作配置
     * 优先使用数据库中的配置，如果不存在则使用注解配置并保存到数据库
     */
    private void initializeOperationConfigs() {
        try {
            // 获取所有已注册的操作类型
            for (String operationType : operationRegistry.getAllOperations()) {
                // 检查数据库中是否已存在该操作的配置
                Optional<OperationConfigData> existingConfig = persistenceService.getOperationConfig(operationType);
                
                if (existingConfig.isPresent()) {
                    // 数据库中已存在配置，使用数据库配置更新内存容器
                    OperationConfigData dbConfig = existingConfig.get();
                    
                    // 同步模型映射到操作注册中心
                    if (dbConfig.getModelName() != null && !dbConfig.getModelName().isEmpty()) {
                        operationRegistry.setModelForOperation(operationType, dbConfig.getModelName());
                        log.info("使用数据库配置初始化操作: {} -> 模型: {}", operationType, dbConfig.getModelName());
                    } else {
                        log.info("使用数据库配置初始化操作: {} (无关联模型)", operationType);
                    }
                } else {
                    // 数据库中不存在配置，从注解获取并保存到数据库
                    Optional<OperationConfigData> dynamicConfig = dynamicOperationConfigService.getOperationConfig(operationType);
                    if (dynamicConfig.isPresent()) {
                        OperationConfigData annotationConfig = dynamicConfig.get();
                        
                        // 保存到数据库
                        persistenceService.saveOperationConfig(operationType, annotationConfig);
                        
                        // 同步模型映射到操作注册中心
                        if (annotationConfig.getModelName() != null && !annotationConfig.getModelName().isEmpty()) {
                            operationRegistry.setModelForOperation(operationType, annotationConfig.getModelName());
                            log.info("从注解初始化操作配置到数据库: {} -> 模型: {}", operationType, annotationConfig.getModelName());
                        } else {
                            log.info("从注解初始化操作配置到数据库: {} (无关联模型)", operationType);
                        }
                    } else {
                        log.debug("操作 {} 没有@AIOp注解配置，跳过初始化", operationType);
                    }
                }
            }
        } catch (Exception e) {
            log.error("初始化操作配置时发生错误: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 转换为OpenAI模型配置
     */
    private OpenAIModelConfig convertToOpenAIConfig(ModelConfigData config) {
        return OpenAIModelConfig.builder()
                .modelName(config.getModelName())
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .defaultMaxTokens(config.getDefaultMaxTokens())
                .defaultTemperature(config.getDefaultTemperature())
                .supportStream(config.getSupportStream())
                .supportJsonOutput(config.getSupportJsonOutput())
                .supportThinking(config.getSupportThinking())
                .additionalHeaders(config.getAdditionalHeaders())
                .description(config.getDescription())
                .provider(config.getProvider())
                .enabled(config.getEnabled())
                .build();
    }
    
    /**
     * 从OpenAI模型配置转换
     */
    private ModelConfigData convertFromOpenAIConfig(OpenAIModelConfig config) {
        ModelConfigData data = new ModelConfigData();
        data.setModelName(config.getModelName());
        data.setBaseUrl(config.getBaseUrl());
        data.setApiKey(config.getApiKey());
        data.setDefaultMaxTokens(config.getDefaultMaxTokens());
        data.setDefaultTemperature(config.getDefaultTemperature());
        data.setSupportStream(config.getSupportStream());
        data.setSupportJsonOutput(config.getSupportJsonOutput());
        data.setSupportThinking(config.getSupportThinking());
        data.setAdditionalHeaders(config.getAdditionalHeaders());
        data.setDescription(config.getDescription());
        data.setProvider(config.getProvider());
        data.setEnabled(config.getEnabled());
        data.updateTimestamp();
        return data;
    }
}