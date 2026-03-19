package edu.jjxy.studyroom.backend.config;

import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shiro配置
 */
@Configuration
public class ShiroConfig {

    @Bean
    public UserRealm userRealm() {
        return new UserRealm();
    }

    @Bean
    public SecurityManager securityManager(UserRealm userRealm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(userRealm);
        return securityManager;
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager, JwtFilter jwtFilter) {
        ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();
        factoryBean.setSecurityManager(securityManager);

        // 添加JWT过滤器
        Map<String, Filter> filterMap = new LinkedHashMap<>();
        filterMap.put("jwt", jwtFilter);
        factoryBean.setFilters(filterMap);

        // 设置过滤链
        Map<String, String> filterChain = new LinkedHashMap<>();

        // 公开接口（无需登录）
        filterChain.put("/api/user/register", "anon");          // 注册
        filterChain.put("/api/user/login", "anon");            // 登录
        filterChain.put("/api/user/sendEmailCode", "anon");    // 发送邮箱验证码
        filterChain.put("/api/user/resetPassword", "anon");    // 找回密码
        filterChain.put("/api/captcha/**", "anon");             // 图形验证码
        filterChain.put("/api/system/time", "anon");            // 服务器时间
        filterChain.put("/ws/**", "anon");                      // WebSocket

        // 管理端接口需要管理员权限
        filterChain.put("/api/admin/**", "jwt");

        // 学生端接口需要登录
        filterChain.put("/api/**", "jwt");

        // 设置登录页面
        factoryBean.setLoginUrl("/api/user/login");

        // 设置未授权页面
        factoryBean.setUnauthorizedUrl("/api/user/unauthorized");

        factoryBean.setFilterChainDefinitionMap(filterChain);

        return factoryBean;
    }
}
