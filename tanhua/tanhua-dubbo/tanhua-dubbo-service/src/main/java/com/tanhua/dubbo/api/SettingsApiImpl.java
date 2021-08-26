package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.domain.db.Settings;
import com.tanhua.dubbo.mapper.SettingsMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class SettingsApiImpl implements SettingsApi {

    @Autowired
    private SettingsMapper settingsMapper;

    /**
     * 通过用户id查询通知设置
     *
     * @param userId
     * @return
     */
    @Override
    public Settings findByUserId(Long userId) {
        QueryWrapper<Settings> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return settingsMapper.selectOne(queryWrapper);
    }

    /**
     * 保存通知设置
     *
     * @param settings
     */
    @Override
    public void save(Settings settings) {
        //1. 通过用户id查询
        Settings settingsInDB = findByUserId(settings.getUserId());
        //2. 存在，则更新
        if(null != settingsInDB){
            settings.setId(settingsInDB.getId());
            // updateById update table set 列=值(不为空) where id=settings.getId()
            settingsMapper.updateById(settings);
        }else {
            //3. 不存在，则添加
            settingsMapper.insert(settings);
        }
    }
}
