package io.github.timemachinelab.core.fingerprint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户指纹服务
 * 基于请求的稳定特征生成指纹（在无可信代理前提下，仅信任直连 IP）
 * @author
 * @date 2025/1/27
 */
@Service
@Slf4j
public class FingerprintService {

    // 指纹到用户信息的映射（内存态，进程重启会丢失；生产可换成持久层或分布式缓存）
    private final Map<String, UserFingerprint> fingerprintMap = new ConcurrentHashMap<>();

    /**
     * 生成用户指纹
     * 仅基于不可伪造的直连 IP 和若干相对稳定的 HTTP 头部（做了规范化）
     */
    public String generateFingerprint(HttpServletRequest request) {
        // 基础字段（均已做规范化）
        String ip = getRemoteIp(request);
        String ua = normalizeUserAgent(request.getHeader("User-Agent"));
        String lang = normalizeAcceptLanguage(request.getHeader("Accept-Language"));
        String enc = normalizeCsvHeader(request.getHeader("Accept-Encoding"));
        String host = normalizeHost(request.getHeader("Host"));

        String raw = "ip:" + ip +
                "|ua:" + ua +
                "|lang:" + lang +
                "|enc:" + enc +
                "|host:" + host;

        // 降低日志敏感度：不打印完整 UA/原串
        if (log.isDebugEnabled()) {
            log.debug("raw fp summary -> ip={}, uaKey={}, lang={}, encKey={}, host={}",
                    ip, shortUaKey(ua), lang, enc.equals("unknown") ? "unknown" : "hashed", host);
        }

        String sha256 = sha256Hex(raw);
        // 默认输出 128 位（32 hex），如需 64 位可改为 substring(0,16)
        String fingerprint = sha256.substring(0, 32);
        log.info("生成用户指纹: {} (ip: {}, port: {})", fingerprint, ip, getClientPort(request));
        return fingerprint;
    }

    /**
     * 获取或创建用户指纹信息
     */
    public UserFingerprint getOrCreateUserFingerprint(HttpServletRequest request) {
        String fingerprint = generateFingerprint(request);
        long now = System.currentTimeMillis();

        return fingerprintMap.compute(fingerprint, (fp, existing) -> {
            if (existing == null) {
                UserFingerprint uf = UserFingerprint.builder()
                        .fingerprint(fp)
                        .firstSeen(now)
                        .lastSeen(now)
                        .visitCount(1)
                        .lastIp(getRemoteIp(request))
                        .userAgent(safeTrim(request.getHeader("User-Agent")))
                        .build();
                log.info("创建新用户指纹: {}", fp);
                return uf;
            } else {
                existing.setLastSeen(now);
                existing.setVisitCount(existing.getVisitCount() + 1);
                existing.setLastIp(getRemoteIp(request));
                // 可选：更新 UA（若你希望记录最近一次 UA）
                String ua = safeTrim(request.getHeader("User-Agent"));
                if (!"unknown".equals(ua)) {
                    existing.setUserAgent(ua);
                }
                log.info("更新现有用户指纹: {}, 访问次数: {}", fp, existing.getVisitCount());
                return existing;
            }
        });
    }

    /**
     * 根据指纹获取用户信息
     */
    public UserFingerprint getUserFingerprintByFingerprint(String fingerprint) {
        return fingerprintMap.get(fingerprint);
    }

    /**
     * 获取指纹统计信息
     */
    public Map<String, Object> getFingerprintStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("totalFingerprints", fingerprintMap.size());
        stats.put("timestamp", System.currentTimeMillis());
        return stats;
    }

    // ============================
    // 内部工具与规范化方法
    // ============================

    // 只信任直连 IP，不解析任何可伪造头
    private String getRemoteIp(HttpServletRequest request) {
        if (request == null) return "unknown";
        String ip = request.getRemoteAddr();
        return ip != null ? ip.trim() : "unknown";
    }

    private int getClientPort(HttpServletRequest request) {
        return request != null ? request.getRemotePort() : -1;
    }

    private String safeTrim(String s) {
        return (s == null || s.isBlank()) ? "unknown" : s.trim();
    }

    // 规范化 UA：可选抽取主浏览器与主版本，降低版本小变动的影响
    private String normalizeUserAgent(String ua) {
        if (ua == null || ua.isBlank()) return "unknown";
        String v = ua.trim();
        String lower = v.toLowerCase();

        String browser = null;
        String version = null;
        String[] markers = {"chrome/", "firefox/", "safari/", "edg/", "edge/", "opera/", "opr/"};
        for (String m : markers) {
            int i = lower.indexOf(m);
            if (i >= 0) {
                browser = m.replace("/", "");
                int j = i + m.length();
                int k = j;
                while (k < lower.length() && (Character.isDigit(lower.charAt(k)) || lower.charAt(k) == '.')) k++;
                String ver = lower.substring(j, k);
                version = ver.isEmpty() ? null : ver.split("\\.")[0]; // 主版本
                break;
            }
        }
        if (browser != null && version != null) {
            return browser + "/" + version;
        }
        // 找不到就返回原 UA，避免过度丢失信息
        return v;
    }

    // 规范化 Accept-Language：取第一项，去掉 ;q= 权重，小写
    private String normalizeAcceptLanguage(String al) {
        if (al == null || al.isBlank()) return "unknown";
        String first = al.split(",")[0].trim();
        int q = first.indexOf(';');
        if (q >= 0) first = first.substring(0, q).trim();
        return first.toLowerCase();
    }

    // 规范化 CSV 类头（如 Accept-Encoding）：去空白、小写、排序，稳定顺序
    private String normalizeCsvHeader(String h) {
        if (h == null || h.isBlank()) return "unknown";
        String[] parts = Arrays.stream(h.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .sorted()
                .toArray(String[]::new);
        if (parts.length == 0) return "unknown";
        return String.join(",", parts);
    }

    private String normalizeHost(String host) {
        if (host == null || host.isBlank()) return "unknown";
        return host.trim().toLowerCase();
    }

    private String shortUaKey(String uaNorm) {
        if (uaNorm == null || uaNorm.isBlank() || "unknown".equalsIgnoreCase(uaNorm)) return "unknown";
        // 仅用于日志摘要
        int max = Math.min(20, uaNorm.length());
        return uaNorm.substring(0, max);
    }

    private String sha256Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hashBytes.length * 2);
            for (byte b : hashBytes) {
                String h = Integer.toHexString(b & 0xff);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // 极少发生：作为回退，返回固定值避免抛异常影响主流程
            log.error("SHA-256 不可用: {}", e.getMessage());
            return "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
        }
    }
}