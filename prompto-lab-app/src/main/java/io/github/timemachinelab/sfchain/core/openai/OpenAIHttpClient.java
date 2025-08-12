package io.github.timemachinelab.sfchain.core.openai;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 描述: OpenAI兼容的HTTP客户端
 * @author suifeng
 * 日期: 2025/8/11
 */
@Slf4j
public class OpenAIHttpClient {
    
    private final String baseUrl;
    private final String apiKey;
    private final Map<String, String> defaultHeaders;
    
    public OpenAIHttpClient(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.apiKey = apiKey;
        this.defaultHeaders = Map.of(
            "Content-Type", "application/json",
            "Authorization", "Bearer " + apiKey
        );
    }
    
    public OpenAIHttpClient(String baseUrl, String apiKey, Map<String, String> additionalHeaders) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.apiKey = apiKey;
        this.defaultHeaders = new HashMap<>();
        this.defaultHeaders.put("Content-Type", "application/json");
        this.defaultHeaders.put("Authorization", "Bearer " + apiKey);
        if (additionalHeaders != null) {
            this.defaultHeaders.putAll(additionalHeaders);
        }
    }
    
    /**
     * 发送聊天完成请求
     */
    public OpenAIResponse chatCompletion(OpenAIRequest request) {
        try {
            // 智能构建endpoint，避免重复的/v1路径
            String endpoint;
            if (baseUrl.endsWith("/v1") || baseUrl.contains("/v1/")) {
                // baseUrl已包含v1路径，直接添加chat/completions
                endpoint = baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "chat/completions";
            } else {
                // baseUrl不包含v1路径，添加完整路径
                endpoint = baseUrl + "/v1/chat/completions";
            }
            String requestBody = JSON.toJSONString(request);
            
            log.debug("发送请求到: {}", endpoint);
            log.debug("请求体: {}", requestBody);
            log.info("构建的API端点: {}", endpoint);
            
             HttpURLConnection connection = createConnection(endpoint);
            
            // 发送请求体
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // 读取响应
            StringBuilder response = new StringBuilder();
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                }
            } else {
                // 读取错误响应
                try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                }
                throw new RuntimeException("HTTP请求失败，状态码: " + responseCode + ", 响应: " + response.toString());
            }

            String responseBody = response.toString();
            log.debug("响应体: {}", responseBody);
            
            return JSON.parseObject(responseBody, OpenAIResponse.class);
            
        } catch (Exception e) {
            log.error("OpenAI API调用失败", e);
            throw new RuntimeException("OpenAI API调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建HTTP连接
     */
    private HttpURLConnection createConnection(String endpoint) throws Exception {
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setConnectTimeout(30000); // 30秒连接超时
        connection.setReadTimeout(120000);   // 120秒读取超时
        
        // 设置请求头
        defaultHeaders.forEach(connection::setRequestProperty);
        
        return connection;
    }
    
    /**
     * 提取响应内容
     */
    public String extractContent(OpenAIResponse response) {
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            return "";
        }
        
        OpenAIResponse.Choice choice = response.getChoices().get(0);
        if (choice.getMessage() != null && choice.getMessage().getContent() != null) {
            return choice.getMessage().getContent();
        }
        
        return "";
    }
}