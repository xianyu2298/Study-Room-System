package edu.jjxy.studyroom.backend.config;

import edu.jjxy.studyroom.backend.common.Constants;
import edu.jjxy.studyroom.backend.common.ResultCode;
import edu.jjxy.studyroom.backend.entity.User;
import edu.jjxy.studyroom.backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

/**
 * Shiro Realm配置
 */
@Slf4j
public class UserRealm extends AuthorizingRealm {

    @Autowired
    @Lazy
    private UserService userService;

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        // 1. JWT 登录时 PrincipalCollection 中存放的是 userId(Long)
        Long userId = principals.oneByType(Long.class);
        if (userId != null) {
            User user = userService.findById(userId);
            if (user != null) {
                return buildAuthzInfo(user);
            }
        }

        // 2. 传统登录时存放的是 User 对象
        User user = (User) principals.getPrimaryPrincipal();
        if (user != null && user.getId() != null) {
            return buildAuthzInfo(user);
        }

        return null;
    }

    /**
     * 构建权限信息
     */
    private SimpleAuthorizationInfo buildAuthzInfo(User user) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

        if (user.getRole() != null) {
            if (user.getRole() == Constants.ROLE_SUPER_ADMIN) {
                info.addRole("super_admin");
                info.addStringPermission("*:*");
            } else if (user.getRole() == Constants.ROLE_ADMIN) {
                info.addRole("admin");
                info.addStringPermission("user:view");
                info.addStringPermission("user:edit");
                info.addStringPermission("room:view");
                info.addStringPermission("room:edit");
                info.addStringPermission("seat:edit");
                info.addStringPermission("reserve:view");
                info.addStringPermission("reserve:manage");
                info.addStringPermission("violation:view");
                info.addStringPermission("notice:view");
                info.addStringPermission("notice:edit");
                info.addStringPermission("config:view");
                info.addStringPermission("config:edit");
                info.addStringPermission("statistics:view");
                info.addStringPermission("log:view");
            } else {
                info.addRole("student");
                info.addStringPermission("room:view");
                info.addStringPermission("reserve:create");
                info.addStringPermission("reserve:view");
                info.addStringPermission("notice:view");
            }
        }

        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        User user;
        String principal;

        if (token instanceof UsernamePasswordToken) {
            // 传统用户名密码登录
            UsernamePasswordToken upToken = (UsernamePasswordToken) token;
            principal = upToken.getUsername();
            user = userService.findByStudentNoOrEmail(principal);
        } else if (token instanceof JwtFilter.JwtToken) {
            // JWT Token 登录（Filter 已验证过有效性，直接取 userId 查询用户）
            JwtFilter.JwtToken jwtToken = (JwtFilter.JwtToken) token;
            user = userService.findById(jwtToken.getUserId());
            principal = "JWT:" + jwtToken.getUserId();
        } else {
            throw new AuthenticationException("不支持的认证方式");
        }

        if (user == null) {
            throw new UnknownAccountException("用户不存在: " + principal);
        }

        if (user.getStatus() != null && user.getStatus() == Constants.STATUS_DISABLED) {
            throw new LockedAccountException("账号已被禁用");
        }
        if (user.getStatus() != null && user.getStatus() == Constants.STATUS_LOCKED) {
            throw new LockedAccountException("账号已被锁定");
        }

        // 将 User 对象作为 principal 存入 Subject
        return new SimpleAuthenticationInfo(user, user.getPassword(), getName());
    }
}
