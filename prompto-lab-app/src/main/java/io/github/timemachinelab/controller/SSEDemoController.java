package io.github.timemachinelab.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Map;

@RestController
@RequestMapping("/api/demo")
@Slf4j
public class SSEDemoController {
    
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    /**
     * 建立SSE连接
     */
    @GetMapping(value = "/sse/{clientId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@PathVariable String clientId) {
        log.info("客户端连接SSE: {}", clientId);
        
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.put(clientId, emitter);
        
        // 连接建立时发送欢迎消息
        try {
            emitter.send(SseEmitter.event()
                .name("connected")
                .data("SSE连接已建立，客户端ID: " + clientId));
        } catch (IOException e) {
            log.error("发送欢迎消息失败: {}", e.getMessage());
        }
        
        // 设置连接事件处理
        emitter.onCompletion(() -> {
            log.info("SSE连接完成: {}", clientId);
            emitters.remove(clientId);
        });
        
        emitter.onTimeout(() -> {
            log.info("SSE连接超时: {}", clientId);
            emitters.remove(clientId);
        });
        
        emitter.onError((ex) -> {
            log.error("SSE连接错误: {} - {}", clientId, ex.getMessage());
            emitters.remove(clientId);
        });
        
        return emitter;
    }
    
    /**
     * 发送消息到指定客户端
     */
    @PostMapping("/send/{clientId}")
    public String sendMessage(@PathVariable String clientId, @RequestParam String message) {
        SseEmitter emitter = emitters.get(clientId);
        
        if (emitter == null) {
            return "客户端未连接: " + clientId;
        }
        
        try {
            emitter.send(SseEmitter.event()
                .name("message")
                .data(message));
            
            log.info("消息已发送到客户端 {}: {}", clientId, message);
            return "消息发送成功";
            
        } catch (IOException e) {
            log.error("发送消息失败: {}", e.getMessage());
            emitters.remove(clientId);
            return "发送失败: " + e.getMessage();
        }
    }
    
    /**
     * 广播消息到所有连接的客户端
     */
    @PostMapping("/broadcast")
    public String broadcast(@RequestParam String message) {
        int successCount = 0;
        int failCount = 0;
        
        for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
            try {
                entry.getValue().send(SseEmitter.event()
                    .name("broadcast")
                    .data(message));
                successCount++;
            } catch (IOException e) {
                log.error("广播消息失败，客户端: {} - {}", entry.getKey(), e.getMessage());
                emitters.remove(entry.getKey());
                failCount++;
            }
        }
        
        return String.format("广播完成 - 成功: %d, 失败: %d", successCount, failCount);
    }
    
    /**
     * 真实流式数据传输 - SSE本身就是流式的
     * 这里演示连续推送数据流的场景
     */
    @PostMapping("/stream/{clientId}")
    public String startStream(@PathVariable String clientId) {
        SseEmitter emitter = emitters.get(clientId);
        
        if (emitter == null) {
            return "客户端未连接: " + clientId;
        }
        
        // 真实流式传输：连续推送数据，每秒一条，共10条
        // 这就是SSE的本质 - 服务器主动推送数据流
        scheduler.schedule(() -> {
            for (int i = 1; i <= 10; i++) {
                final int count = i;
                scheduler.schedule(() -> {
                    try {
                        // 直接通过SSE流式推送数据
                        emitter.send(SseEmitter.event()
                            .name("stream")
                            .data("实时数据流 #" + count + " - 时间戳: " + System.currentTimeMillis()));
                        
                        // 最后一条数据后发送完成通知
                        if (count == 10) {
                            scheduler.schedule(() -> {
                                try {
                                    emitter.send(SseEmitter.event()
                                        .name("stream_complete")
                                        .data("数据流传输完成"));
                                } catch (IOException e) {
                                    log.error("发送完成通知失败: {}", e.getMessage());
                                }
                            }, 1, TimeUnit.SECONDS);
                        }
                        
                    } catch (IOException e) {
                        log.error("流式数据推送失败: {}", e.getMessage());
                        emitters.remove(clientId);
                    }
                }, count, TimeUnit.SECONDS);
            }
        }, 0, TimeUnit.SECONDS);
        
        return "SSE数据流已开始推送";
    }
    
    /**
     * 获取当前连接状态
     */
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new ConcurrentHashMap<>();
        status.put("connectedClients", emitters.keySet());
        status.put("totalConnections", emitters.size());
        status.put("timestamp", System.currentTimeMillis());
        return status;
    }
}