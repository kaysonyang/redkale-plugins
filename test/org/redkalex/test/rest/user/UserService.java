/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redkalex.test.rest.user;

import org.redkalex.test.rest.base.UserInfo;
import org.redkale.service.Service;

/**
 * 简单的定义UserService接口
 *
 * @author zhangjx
 */
public class UserService implements Service {

    //根据登录态获取当前用户信息
    public UserInfo current(String sessionid) {
        return null;
    }
}
