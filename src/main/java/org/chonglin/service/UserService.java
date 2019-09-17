package org.chonglin.service;

import org.chonglin.bean.UserDO;
import org.chonglin.form.UserForm;

import java.util.Map;

public interface UserService {
    UserDO getByUsername(String username);


    UserDO queryByColumn(Map<String,Object> map);

    Integer addUser(UserForm userForm);
}
