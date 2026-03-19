package edu.jjxy.studyroom.backend.config;

import io.jsonwebtoken.Claims;
import lombok.Data;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

/**
 * JWT认证信息
 */
@Data
public class JwtAuthenticationToken implements AuthenticationToken {

    private String token;
    private Long userId;
    private Integer role;

    public JwtAuthenticationToken(String token, Long userId, Integer role) {
        this.token = token;
        this.userId = userId;
        this.role = role;
    }

    @Override
    public Object getPrincipal() {
        return userId;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}
