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
        // 获取当前登录用户
        User user = (User) principals.getPrimaryPrincipal();
        if (user == null) {
            return null;
        }

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

        // 设置角色
        if (user.getRole() != null) {
            if (user.getRole() == Constants.ROLE_SUPER_ADMIN) {
                info.addRole("super_admin");
                info.addStringPermission("*:*"); // 超级管理员拥有所有权限
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
        UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) token;
        String username = usernamePasswordToken.getUsername();

        // 根据用户名获取用户
        User user = userService.findByStudentNoOrEmail(username);
        if (user == null) {
            throw new UnknownAccountException("用户不存在");
        }

        // 检查账号状态
        if (user.getStatus() != null && user.getStatus() == Constants.STATUS_DISABLED) {
            throw new LockedAccountException("账号已被禁用");
        }

        if (user.getStatus() != null && user.getStatus() == Constants.STATUS_LOCKED) {
            throw new LockedAccountException("账号已被锁定");
        }

        // 简单密码验证（实际使用BCrypt等加密方式）
        return new SimpleAuthenticationInfo(user, user.getPassword(), getName());
    }
}
