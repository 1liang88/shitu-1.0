package com.liang.shituapi.service.impl;

import com.liang.shituapi.dao.ReportMapper;
import com.liang.shitucommon.entity.Place;
import com.liang.shitucommon.entity.Report;
import com.liang.shituapi.exception.BusinessException;
import com.liang.shituapi.service.PlaceService;
import com.liang.shituapi.service.ReportService;
import com.liang.shitucommon.PageRequest;
import com.liang.shitucommon.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private PlaceService placeService;

    @Override
    public Report getById(Integer id) {
        if (id == null) throw new BusinessException("ID不能为空");
        return reportMapper.selectById(id);
    }

    @Override
    public List<Report> getAll() {
        return reportMapper.selectAll();
    }

    @Override
    public boolean add(Report report) {
        if (report == null) throw new BusinessException("参数不能为空");
        if (report.getPlaceName() == null || report.getPlaceName().trim().isEmpty())
            throw new BusinessException("点位名称不能为空");
        if (report.getReportType() == null) throw new BusinessException("请选择上报类型");

        report.setStatus(0);
        if (report.getReporterName() == null) report.setReporterName("匿名用户");

        return reportMapper.insert(report) > 0;
    }

    @Override
    public boolean addBatch(List<Report> list) {
        if (list == null || list.isEmpty()) throw new BusinessException("列表不能为空");
        for (Report report : list) {
            if (report.getStatus() == null) report.setStatus(0);
            if (report.getReporterName() == null) report.setReporterName("匿名用户");
        }
        return reportMapper.batchInsert(list) > 0;
    }

    @Override
    public boolean update(Report report) {
        if (report == null || report.getId() == null) throw new BusinessException("参数错误");
        Report old = reportMapper.selectById(report.getId());
        if (old == null) throw new BusinessException("上报记录不存在");
        if (old.getStatus() != 0) throw new BusinessException("已审核的记录不能修改");
        return reportMapper.updateById(report) > 0;
    }

    @Override
    public boolean deleteById(Integer id) {
        if (id == null) throw new BusinessException("ID不能为空");
        Report old = reportMapper.selectById(id);
        if (old != null && old.getStatus() != 0) throw new BusinessException("已审核的记录不能删除");
        return reportMapper.deleteById(id) > 0;
    }

    @Override
    public boolean deleteBatch(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) throw new BusinessException("ID列表不能为空");
        for (Integer id : ids) {
            Report old = reportMapper.selectById(id);
            if (old != null && old.getStatus() != 0)
                throw new BusinessException("包含已审核的记录，不能批量删除");
        }
        return reportMapper.batchDeleteByIds(ids) > 0;
    }

    @Override
    public PageResult<Report> page(PageRequest pageRequest, String keyword, Integer categoryId, Integer reportType, Integer status) {
        int offset = (pageRequest.getPageNum() - 1) * pageRequest.getPageSize();
        List<Report> list = reportMapper.selectPage(keyword, categoryId, reportType, status, offset, pageRequest.getPageSize());
        Long total = reportMapper.selectCount(keyword, categoryId, reportType, status);
        int pages = (int) Math.ceil((double) total / pageRequest.getPageSize());

        PageResult<Report> result = new PageResult<>();
        result.setList(list); result.setTotal(total); result.setPages(pages);
        result.setPageNum(pageRequest.getPageNum()); result.setPageSize(pageRequest.getPageSize());
        return result;
    }

//    @Override
//    public boolean audit(Integer id, Integer status, String remark, Integer auditBy) {
//        if (id == null || status == null || auditBy == null) throw new BusinessException("参数不能为空");
//        Report report = reportMapper.selectById(id);
//        if (report == null) throw new BusinessException("上报记录不存在");
//        if (report.getStatus() != 0) throw new BusinessException("该记录已经审核过了");
//
//        LocalDateTime now = LocalDateTime.now();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        String auditTime = now.format(formatter);
//        int result = reportMapper.audit(id, status, remark, auditBy, auditTime);
//
//        if (result > 0 && status == 1 && report.getReportType() == 1) {
//            createPlaceFromReport(report);
//        }
//        return result > 0;
//    }
    //测试日志
@Override
public boolean audit(Integer id, Integer status, String remark, Integer auditBy) {
    log.info("开始审核上报，ID: {}, 状态: {}, 审核人: {}", id, status, auditBy);

    if (id == null || status == null || auditBy == null) {
        log.warn("审核参数不完整: id={}, status={}, auditBy={}", id, status, auditBy);
        throw new BusinessException("参数不能为空");
    }

    // 查询上报记录
    Report report = reportMapper.selectById(id);
    if (report == null) {
        log.warn("上报记录不存在，ID: {}", id);
        throw new BusinessException("上报记录不存在");
    }

    log.info("上报记录详情: {}", report);

    // 如果已经审核过，不能再次审核
    if (report.getStatus() != 0) {
        log.warn("记录已审核，当前状态: {}", report.getStatus());
        throw new BusinessException("该记录已经审核过了");
    }

    // 审核时间
    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String auditTime = now.format(formatter);

    // 执行审核
    int result = reportMapper.audit(id, status, remark, auditBy, auditTime);
    log.info("审核更新结果: {}", result);

    // 如果审核通过且是新增类型，则创建点位
    if (result > 0 && status == 1 && report.getReportType() == 1) {
        log.info("开始从上报创建点位，上报ID: {}", id);
        createPlaceFromReport(report);
    }

    return result > 0;
}

    @Override
    public boolean batchAudit(List<Integer> ids, Integer status, String remark, Integer auditBy) {
        if (ids == null || ids.isEmpty() || status == null || auditBy == null)
            throw new BusinessException("参数不能为空");
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String auditTime = now.format(formatter);
        int result = reportMapper.batchAudit(ids, status, remark, auditBy, auditTime);
        if (result > 0 && status == 1) {
            for (Integer id : ids) {
                Report report = reportMapper.selectById(id);
                if (report != null && report.getReportType() == 1) {
                    createPlaceFromReport(report);
                }
            }
        }
        return result > 0;
    }

    @Override
    public boolean batchApprove(List<Integer> ids, String remark, Integer auditBy) {
        return batchAudit(ids, 1, remark, auditBy);
    }

    @Override
    public boolean batchReject(List<Integer> ids, String remark, Integer auditBy) {
        return batchAudit(ids, 2, remark, auditBy);
    }

    @Override
    public Long countByStatus(Integer status) {
        return reportMapper.countByStatus(status);
    }

    @Override
    public List<Map<String, Object>> countByType() {
        List<ReportMapper.TypeStat> stats = reportMapper.countByType();
        List<Map<String, Object>> result = new ArrayList<>();
        for (ReportMapper.TypeStat stat : stats) {
            Map<String, Object> map = new HashMap<>();
            map.put("reportType", stat.getReportType());
            map.put("typeName", stat.getTypeName());
            map.put("count", stat.getCount());
            result.add(map);
        }
        return result;
    }

    @Override
    public Long getPendingCount() {
        return reportMapper.countByStatus(0);
    }

    @Override
    public boolean exists(Integer id) {
        return reportMapper.selectById(id) != null;
    }

    @Override
    public List<Report> getUserReports(String reporterPhone) {
        if (reporterPhone == null || reporterPhone.trim().isEmpty())
            throw new BusinessException("手机号不能为空");
        return reportMapper.selectByReporterPhone(reporterPhone);
    }

    private void createPlaceFromReport(Report report) {
        try {
            Place place = new Place();
            place.setName(report.getPlaceName());
            place.setCategoryId(report.getCategoryId());
            place.setAddress(report.getAddress());
            place.setLongitude(report.getLongitude());
            place.setLatitude(report.getLatitude());
            place.setImages(report.getImages());
            place.setDescription(report.getContent());
            place.setCreateBy("系统(来自上报)");

            // 调用修改后的方法，传入reportId
            boolean success = placeService.createFromReport(place, report.getId());

            if (success) {
                log.info("从上报记录创建点位成功，上报ID：{}，点位名称：{}",
                        report.getId(), report.getPlaceName());
            } else {
                log.error("从上报记录创建点位失败，上报ID：{}", report.getId());
            }
        } catch (Exception e) {
            log.error("从上报记录创建点位异常", e);
        }
    }
    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 按状态统计
        stats.put("pending", countByStatus(0));
        stats.put("approved", countByStatus(1));
        stats.put("rejected", countByStatus(2));

        // 按类型统计
        stats.put("byType", countByType());

        // 今日新增
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime tomorrow = today.plusDays(1);
        // 这里需要添加一个根据时间范围查询的方法，暂时用0代替
        stats.put("todayCount", 0);

        // 本周新增
        stats.put("weekCount", 0);

        // 本月新增
        stats.put("monthCount", 0);

        return stats;
    }
}