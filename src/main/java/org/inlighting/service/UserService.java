package org.inlighting.service;

import org.inlighting.bean.UserDO;

public interface UserService {
    UserDO getByUsername(String username);
}
