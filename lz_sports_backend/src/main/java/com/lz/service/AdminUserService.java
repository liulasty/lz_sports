package com.lz.service;

import com.lz.common.result.PageResult;
import com.lz.dto.UserQueryDTO;

public interface AdminUserService {
    PageResult getUsers(UserQueryDTO queryDTO);
    void changeRole(Long id, String role);
    void disableUser(Long id);
    void enableUser(Long id);
}
