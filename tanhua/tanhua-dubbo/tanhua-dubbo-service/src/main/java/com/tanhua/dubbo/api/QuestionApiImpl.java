package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.domain.db.Question;
import com.tanhua.domain.db.Settings;
import com.tanhua.dubbo.mapper.QuestionMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class QuestionApiImpl implements QuestionApi {

    @Autowired
    private QuestionMapper questionMapper;

    /**
     * 通过用户is查询陌生人问题
     *
     * @param userId
     * @return
     */
    @Override
    public Question findByUserId(Long userId) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return questionMapper.selectOne(queryWrapper);
    }

    /**
     * 保存陌生人问题
     *
     * @param question
     */
    @Override
    public void save(Question question) {
        Question questionInDB = findByUserId(question.getUserId());
        if(null == questionInDB){
            // 不存在
            questionMapper.insert(question);
        }else{
            question.setId(questionInDB.getId());
            // 存在，则要更新
            questionMapper.updateById(question);
        }
    }
}
