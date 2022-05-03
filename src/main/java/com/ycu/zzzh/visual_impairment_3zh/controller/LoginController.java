package com.ycu.zzzh.visual_impairment_3zh.controller;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import com.ycu.zzzh.visual_impairment_3zh.jwt.JwtUtils;
import com.ycu.zzzh.visual_impairment_3zh.model.domain.User;
import com.ycu.zzzh.visual_impairment_3zh.model.result.Msg;
import com.ycu.zzzh.visual_impairment_3zh.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;


@RestController
public class LoginController {

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户登录
     * @param userName
     * @param password
     * @param response
     * @return
     */
    @PostMapping(value = "/login")
    public Object userLogin(@RequestParam(name = "username", required = true) String userName,
                            @RequestParam(name = "password", required = true) String password, ServletResponse response) {

        // 获取当前用户主体
        Subject subject = SecurityUtils.getSubject();
        String msg = null;
        boolean loginSuccess = false;
        // 将用户名和密码封装成 UsernamePasswordToken 对象
        UsernamePasswordToken token = new UsernamePasswordToken(userName, password);
        try {
            subject.login(token);
            msg = "登录成功";
            loginSuccess = true;
        } catch (UnknownAccountException uae) { // 账号不存在
            msg = "用户名与密码不匹配，请检查后重新输入！";
        } catch (LockedAccountException lae) { // 账号已被锁定
            msg = "该用户被封号,暂时无法登录！";
        } catch (IncorrectCredentialsException ice) { // 账号与密码不匹配
            msg = "用户名与密码不匹配，请检查后重新输入！";
        }  catch (AuthenticationException ae) { // 其他身份验证异常
            msg = "登录异常，请联系管理员！";
        }
        Msg<Object> ret = new Msg<Object>();
        if (loginSuccess) {
            // 若登录成功，签发 JWT token
            String jwtToken = JwtUtils.sign(userName, JwtUtils.SECRET);
            // 将签发的 JWT token 设置到 HttpServletResponse 的 Header 中
            ((HttpServletResponse) response).setHeader(JwtUtils.AUTH_HEADER, jwtToken);
            //
            ret.setErrCode(0);
            ret.setMsg(msg);
            return ret;
        } else {
            ret.setErrCode(401);
            ret.setMsg(msg);
            return ret;
        }

    }

    /**
     * 用户退出登录
     * @return
     */
    @GetMapping("/logout")
    public Object logout() {
        Msg<Object> ret = new Msg<Object>();
        ret.setErrCode(0);
        ret.setMsg("退出登录");
        return ret;
    }

    /**
     * 用户注册
     * @param user
     * @return
     */
    @PostMapping("/registered")
    public Object registered(@RequestBody User user){
        Msg<Object> ret = new Msg<Object>();
        try {
            int i = userService.addUserInfoService(user);
            ret.setErrCode(0);
            ret.setMsg("注册成功");
        }catch (Exception e){
            ret.setErrCode(401);
            ret.setMsg("注册失败");
        }
        return ret;

    }
}
