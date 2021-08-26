package com.tanhua.dubbo.api;

import com.tanhua.domain.db.Settings;

public interface SettingsApi {
    /**
     * 通过用户id查询通知设置
     * @param userId
     * @return
     */
    Settings findByUserId(Long userId);

    /**
     * 保存通知设置
     * @param settings
     */
    void save(Settings settings);
}
