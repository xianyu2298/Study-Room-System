package edu.jjxy.studyroom.backend.config;

import edu.jjxy.studyroom.backend.common.ResultCode;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT过滤器
 */
@Slf4j
@Component
public class JwtFilter extends BasicHttpAuthenticationFilter {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 判断是否允许访问
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        // 尝试获取Token
        String token = getJwtToken(request);
        if (token != null) {
            // 验证Token
            try {
                Claims claims = jwtUtil.parseToken(token);
                Long userId = claims.get("userId", Long.class);
                Integer role = claims.get("role", Integer.class);

                // 将用户信息存入Session
                getSubject(request, response).login(new JwtToken(token, userId, role));
                return true;
            } catch (Exception e) {
                log.warn("JWT验证失败 - error: {}", e.getMessage());
                return false;
            }
        }

        // 如果是OPTIONS请求，直接放行
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            return true;
        }

        // 返回401
        return false;
    }

    /**
     * 访问被拒绝时的处理
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletResponse httpResponse = WebUtils.toHttp(response);

        // 设置响应头
        httpResponse.setContentType("application/json;charset=UTF-8");
        httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());

        // 返回JSON响应
        String json = "{\"code\":" + ResultCode.TOKEN_INVALID.getCode() +
                ",\"msg\":\"" + ResultCode.TOKEN_INVALID.getMessage() +
                "\",\"data\":null,\"timestamp\":" + System.currentTimeMillis() + "}";
        httpResponse.getWriter().write(json);

        return false;
    }

    /**
     * 跨域处理
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        HttpServletResponse httpResponse = WebUtils.toHttp(response);

        // 允许跨域
        httpResponse.setHeader("Access-Control-Allow-Origin", httpRequest.getHeader("Origin"));
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, X-Requested-With");
        httpResponse.setHeader("Access-Control-Expose-Headers", "Authorization");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");

        // 如果是OPTIONS请求，直接返回
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpStatus.OK.value());
            return false;
        }

        return super.preHandle(request, response);
    }

    /**
     * 获取JWT Token
     */
    private String getJwtToken(ServletRequest request) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        String token = httpRequest.getHeader(jwtUtil.getHeaderName());
        if (token != null && token.startsWith(jwtUtil.getTokenPrefix())) {
            return token.substring(jwtUtil.getTokenPrefix().length() + 1);
        }
        return null;
    }

    /**
     * JWT Token对象（实现 Shiro AuthenticationToken）
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class JwtToken implements AuthenticationToken {
        private String token;
        private Long userId;
        private Integer role;

        @Override
        public Object getPrincipal() {
            return userId;
        }

        @Override
        public Object getCredentials() {
            return token;
        }
    }
}
