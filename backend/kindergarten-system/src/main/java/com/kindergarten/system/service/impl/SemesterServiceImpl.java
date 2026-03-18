package com.kindergarten.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kindergarten.system.common.exception.BusinessException;
import com.kindergarten.system.common.result.ResultCode;
import com.kindergarten.system.entity.Semester;
import com.kindergarten.system.mapper.SemesterMapper;
import com.kindergarten.system.service.SemesterService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SemesterServiceImpl extends ServiceImpl<SemesterMapper, Semester> implements SemesterService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setCurrent(Long id) {
        Semester target = getById(id);
        if (target == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        update(new LambdaUpdateWrapper<Semester>()
                .set(Semester::getIsCurrent, 0)
                .eq(Semester::getIsCurrent, 1));
        target.setIsCurrent(1);
        updateById(target);
    }

    @Override
    public Semester findCurrent() {
        return getOne(new LambdaQueryWrapper<Semester>()
                .eq(Semester::getIsCurrent, 1)
                .last("LIMIT 1"));
    }

    @Override
    public IPage<Semester> pageList(String semesterName, Long page, Long pageSize) {
        long current = page == null || page < 1 ? 1 : page;
        long size = pageSize == null || pageSize < 1 ? 10 : pageSize;

        Page<Semester> semesterPage = new Page<>(current, size);
        return page(semesterPage, new LambdaQueryWrapper<Semester>()
                .like(semesterName != null && !semesterName.isBlank(), Semester::getSemesterName, semesterName)
                .orderByDesc(Semester::getIsCurrent)
                .orderByDesc(Semester::getId));
    }
}