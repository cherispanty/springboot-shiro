package org.chonglin.service;

import org.chonglin.bean.UserDO;

public interface UserService {
    UserDO getByUsername(String username);
}
