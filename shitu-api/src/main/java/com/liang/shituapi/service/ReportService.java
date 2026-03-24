package com.liang.shituapi.service;

import com.liang.shitucommon.entity.Report;
import com.liang.shitucommon.PageRequest;
import com.liang.shitucommon.PageResult;
import java.util.List;
import java.util.Map;

public interface ReportService extends BaseService<Report, Integer> {
    PageResult<Report> page(PageRequest pageRequest, String keyword, Integer categoryId, Integer reportType, Integer status);
    boolean audit(Integer id, Integer status, String remark, Integer auditBy);
    boolean batchAudit(List<Integer> ids, Integer status, String remark, Integer auditBy);
    boolean batchApprove(List<Integer> ids, String remark, Integer auditBy);
    boolean batchReject(List<Integer> ids, String remark, Integer auditBy);
    Long countByStatus(Integer status);
    List<Map<String, Object>> countByType();
    Long getPendingCount();
    boolean exists(Integer id);
    List<Report> getUserReports(String reporterPhone);
    // 在 ReportService.java 中添加
    Map<String, Object> getStatistics();
}