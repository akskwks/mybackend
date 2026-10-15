package com.app.mybackend.log.filter;

import com.app.mybackend.log.service.SystemLogService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class LogFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(LogFilter.class);
    private static final Set<String> SENSITIVE = Set.of(
            "password", "passwordconfirm", "token", "authorization", "accesstoken",
            "refreshtoken", "secret", "apikey", "cookie", "sessionid");
    private final SystemLogService service;
    private final JsonMapper jsonMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        if (path.equals("/api/ai/conversations") && method.equals("POST")) return false;
        if (path.matches("/api/ai/conversations/\\d+") &&
                (method.equals("PUT") || method.equals("DELETE"))) return false;
        if (path.equals("/api/calendar/events") || path.matches("/api/calendar/events/\\d+"))
            return !Set.of("GET", "POST", "PUT", "DELETE").contains(method);
        if (path.equals("/api/memos") || path.matches("/api/memos/\\d+"))
            return !Set.of("GET", "POST", "PUT", "DELETE").contains(method);
        if (path.equals("/api/projects") || path.matches("/api/projects/\\d+"))
            return !Set.of("GET", "POST", "PUT", "DELETE").contains(method);
        if (path.equals("/api/works") || path.matches("/api/works/\\d+") ||
                path.equals("/api/works/with-files") || path.matches("/api/works/\\d+/with-files"))
            return !Set.of("GET", "POST", "PUT", "DELETE").contains(method);
        return true;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userNum)) {
            chain.doFilter(request, response);
            return;
        }

        LocalDateTime requestAt = LocalDateTime.now();
        ContentCachingRequestWrapper cachedRequest = new ContentCachingRequestWrapper(request, 262_144);
        ContentCachingResponseWrapper cachedResponse = new ContentCachingResponseWrapper(response);
        Exception failure = null;
        try {
            chain.doFilter(cachedRequest, cachedResponse);
        } catch (ServletException | IOException | RuntimeException ex) {
            failure = ex;
            throw ex;
        } finally {
            LocalDateTime responseAt = LocalDateTime.now();
            try {
                String requestJson = safeRequestJson(cachedRequest);
                String responseJson = responseJson(cachedResponse, failure);
                service.record(userNum, request.getMethod(), request.getRequestURI(),
                        failure == null && cachedResponse.getStatus() >= 200 && cachedResponse.getStatus() < 300,
                        requestJson, requestAt, responseJson, responseAt);
            } catch (Exception logFailure) {
                logger.warn("시스템 로그 저장에 실패했습니다: {} {}", request.getMethod(), request.getRequestURI(), logFailure);
            } finally {
                cachedResponse.copyBodyToResponse();
            }
        }
    }

    private String safeRequestJson(ContentCachingRequestWrapper request) {
        try {
            return requestJson(request);
        } catch (Exception ex) {
            logger.debug("로그 요청 본문을 읽지 못했습니다.", ex);
            return null;
        }
    }

    private String requestJson(ContentCachingRequestWrapper request) throws Exception {
        Map<String, Object> query = new LinkedHashMap<>();
        request.getParameterMap().forEach((key, values) ->
                query.put(key, values.length == 1 ? values[0] : java.util.Arrays.asList(values)));
        String contentType = request.getContentType();
        if (contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith(MediaType.MULTIPART_FORM_DATA_VALUE)) {
            Map<String, Object> multipart = new LinkedHashMap<>();
            List<Map<String, Object>> files = new ArrayList<>();
            for (Part part : request.getParts()) {
                if (part.getSubmittedFileName() != null) {
                    files.add(Map.of("fileName", part.getSubmittedFileName(),
                            "size", part.getSize(), "contentType", part.getContentType() == null ? "" : part.getContentType()));
                } else if (part.getName().equals("work")) {
                    multipart.put("work", parseOrMask(new String(part.getInputStream().readAllBytes(), StandardCharsets.UTF_8)));
                }
            }
            if (!files.isEmpty()) multipart.put("files", files);
            if (!query.isEmpty()) multipart.put("parameters", query);
            return jsonMapper.writeValueAsString(mask(multipart));
        }
        byte[] bytes = request.getContentAsByteArray();
        if (bytes.length == 0 && query.isEmpty()) return null;
        Object body = bytes.length == 0 ? null : parseOrMask(new String(bytes, StandardCharsets.UTF_8));
        if (query.isEmpty()) return jsonMapper.writeValueAsString(body);
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("query", query);
        if (body != null) value.put("body", body);
        return jsonMapper.writeValueAsString(mask(value));
    }

    private String responseJson(ContentCachingResponseWrapper response, Exception failure) {
        if (failure != null) return jsonMapper.writeValueAsString(Map.of("error", failure.getClass().getSimpleName()));
        byte[] bytes = response.getContentAsByteArray();
        if (bytes.length == 0) return null;
        String body = new String(bytes, StandardCharsets.UTF_8);
        return jsonMapper.writeValueAsString(parseOrMask(body));
    }

    private Object parseOrMask(String raw) {
        try {
            return mask(jsonMapper.readValue(raw, Object.class));
        } catch (Exception ex) {
            return Map.of("omitted", "JSON 형식이 아닌 데이터는 기록하지 않습니다.");
        }
    }

    private Object mask(Object value) {
        if (value instanceof Map<?, ?> source) {
            Map<String, Object> result = new LinkedHashMap<>();
            source.forEach((key, item) -> {
                String name = String.valueOf(key);
                String normalized = name.replaceAll("[_-]", "").toLowerCase(Locale.ROOT);
                result.put(name, SENSITIVE.contains(normalized) ? "******" : mask(item));
            });
            return result;
        }
        if (value instanceof List<?> source) return source.stream().map(this::mask).toList();
        return value;
    }
}
