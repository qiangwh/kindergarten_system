/**
 * 班级 Mapper 接口
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
package com.kindergarten.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kindergarten.system.entity.ClassInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClassInfoMapper extends BaseMapper<ClassInfo> {

}