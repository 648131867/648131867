package com.tanhua.manage.service;

import com.tanhua.manage.domain.Log;
import com.tanhua.manage.mapper.LogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * log 业务
 */
@Service
public class LogService {

    @Autowired
    private LogMapper logMapper;

    /**
     * 添加日志
     * @param log
     */
    public void add(Log log) {
        logMapper.insert(log);
    }
}
