package com.lifepilot.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 为每个 HTTP 请求分配请求标识并记录基础访问日志。
 */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    /**
     * 请求标识头名称。
     */
    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    /**
     * MDC 中保存请求标识的键。
     */
    public static final String REQUEST_ID_MDC_KEY = "requestId";

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    /**
     * 创建请求日志过滤器。
     */
    public RequestLoggingFilter() {
    }

    /**
     * 为请求补充 request id，执行后记录方法、路径和状态码。
     *
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param filterChain 过滤器链
     * @throws ServletException 下游处理失败时抛出
     * @throws IOException 下游读写失败时抛出
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String requestId = resolveRequestId(request);
        MDC.put(REQUEST_ID_MDC_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            log.info(
                    "http_request requestId={} method={} path={} status={}",
                    requestId,
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus()
            );
            MDC.remove(REQUEST_ID_MDC_KEY);
        }
    }

    private String resolveRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return requestId;
    }
}
