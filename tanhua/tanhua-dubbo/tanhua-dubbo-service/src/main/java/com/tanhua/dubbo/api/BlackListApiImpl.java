package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.domain.db.BlackList;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.mapper.BlackListMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class BlackListApiImpl implements BlackListApi {

    @Autowired
    private BlackListMapper blackListMapper;

    /**
     * 通过登陆用户id，分页查询登陆用户的黑名单列表
     *
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageResult findPageByUserId(Long userId, Long page, Long pageSize) {
        // 构建分页对象
        IPage<BlackList> iPage = new Page<>(page, pageSize);
        // 构建查询条件
        QueryWrapper<BlackList> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("created");
        // 查询
        blackListMapper.selectPage(iPage, queryWrapper);
        // 构建分页pageResult
        PageResult pageResult = new PageResult();
        pageResult.setItems(iPage.getRecords());
        pageResult.setPage(page);
        pageResult.setPagesize(pageSize);
        pageResult.setCounts(iPage.getTotal());
        pageResult.setPages(iPage.getPages());
        return pageResult;
    }

    /**
     * 移除黑名单
     *
     * @param loginUserId
     * @param blackUserId
     */
    @Override
    public void delete(Long loginUserId, Long blackUserId) {
        QueryWrapper<BlackList> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", loginUserId);
        queryWrapper.eq("black_user_id", blackUserId);
        blackListMapper.delete(queryWrapper);
    }
}
