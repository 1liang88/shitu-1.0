package com.liang.shituapi.dao;


import com.liang.shitucommon.entity.Report;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户上报数据访问接口
 */
@Mapper
public interface ReportMapper extends BaseMapper<Report, Integer> {

    /**
     * 分页查询上报列表
     */
    List<com.liang.shitucommon.entity.Report> selectPage(@Param("keyword") String keyword,
                                                         @Param("categoryId") Integer categoryId,
                                                         @Param("reportType") Integer reportType,
                                                         @Param("status") Integer status,
                                                         @Param("offset") Integer offset,
                                                         @Param("limit") Integer limit);

    /**
     * 查询总数
     */
    Long selectCount(@Param("keyword") String keyword,
                     @Param("categoryId") Integer categoryId,
                     @Param("reportType") Integer reportType,
                     @Param("status") Integer status);

    /**
     * 审核上报
     */
    int audit(@Param("id") Integer id,
              @Param("status") Integer status,
              @Param("auditRemark") String auditRemark,
              @Param("auditBy") Integer auditBy,
              @Param("auditTime") String auditTime);

    /**
     * 批量审核
     */
    int batchAudit(@Param("ids") List<Integer> ids,
                   @Param("status") Integer status,
                   @Param("auditRemark") String auditRemark,
                   @Param("auditBy") Integer auditBy,
                   @Param("auditTime") String auditTime);

    /**
     * 根据状态统计数量
     */
    Long countByStatus(@Param("status") Integer status);

    /**
     * 根据类型统计数量
     */
    List<TypeStat> countByType();

    /**
     * 根据手机号查询上报记录
     */
    List<Report> selectByReporterPhone(@Param("reporterPhone") String reporterPhone);

    /**
     * 今日新增数量
     */
    Long countToday();

    /**
     * 本周新增数量
     */
    Long countThisWeek();

    /**
     * 本月新增数量
     */
    Long countThisMonth();
    /**
     * 内部类：类型统计
     */
    class TypeStat {
        private Integer reportType;
        private String typeName;
        private Integer count;
        // getters and setters
        public Integer getReportType() { return reportType; }
        public void setReportType(Integer reportType) { this.reportType = reportType; }
        public String getTypeName() { return typeName; }
        public void setTypeName(String typeName) { this.typeName = typeName; }
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }

}
