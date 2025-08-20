package io.github.timemachinelab.entity.resp;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 统一API响应结果类
 * 
 * @param <T> 数据类型
 * @author suifeng
 * @date 2025/1/20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResult<T> {
    
    /**
     * 响应状态码
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 成功响应
     */
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(200, "操作成功", data, System.currentTimeMillis(), true);
    }
    
    /**
     * 成功响应（自定义消息）
     */
    public static <T> ApiResult<T> success(String message, T data) {
        return new ApiResult<>(200, message, data, System.currentTimeMillis(), true);
    }
    
    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResult<T> success(String message) {
        return new ApiResult<>(200, message, null, System.currentTimeMillis(), true);
    }
    
    /**
     * 失败响应
     */
    public static <T> ApiResult<T> error(String message) {
        return new ApiResult<>(400, message, null, System.currentTimeMillis(), false);
    }
    
    /**
     * 失败响应（自定义状态码）
     */
    public static <T> ApiResult<T> error(Integer code, String message) {
        return new ApiResult<>(code, message, null, System.currentTimeMillis(), false);
    }
    
    /**
     * 参数校验失败
     */
    public static <T> ApiResult<T> validateError(String message) {
        return new ApiResult<>(400, message, null, System.currentTimeMillis(), false);
    }
    
    /**
     * 服务器内部错误
     */
    public static <T> ApiResult<T> serverError(String message) {
        return new ApiResult<>(500, message, null, System.currentTimeMillis(), false);
    }
}