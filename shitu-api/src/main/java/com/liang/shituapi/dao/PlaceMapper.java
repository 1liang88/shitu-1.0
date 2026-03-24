package com.liang.shituapi.dao;


import com.liang.shitucommon.entity.Place;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 设施点位数据访问接口
 */
@Mapper
public interface PlaceMapper extends BaseMapper<Place, Integer> {

    /**
     * 根据分类查询点位
     */
    List<com.liang.shitucommon.entity.Place> selectByCategoryId(@Param("categoryId") Integer categoryId,
                                                                @Param("status") Integer status);

    /**
     * 分页查询点位列表
     */
    List<com.liang.shitucommon.entity.Place> selectPage(@Param("keyword") String keyword,
                                                        @Param("categoryId") Integer categoryId,
                                                        @Param("status") Integer status,
                                                        @Param("offset") Integer offset,
                                                        @Param("limit") Integer limit);

    /**
     * 查询总数
     */
    Long selectCount(@Param("keyword") String keyword,
                     @Param("categoryId") Integer categoryId,
                     @Param("status") Integer status);

    /**
     * 查询附近点位
     */
    List<com.liang.shitucommon.entity.Place> selectNearby(@Param("longitude") BigDecimal longitude,
                                                          @Param("latitude") BigDecimal latitude,
                                                          @Param("distance") Double distance,  // 距离（公里）
                                                          @Param("categoryId") Integer categoryId,
                                                          @Param("limit") Integer limit);

    /**
     * 更新访问次数
     */
    int incrementVisitCount(@Param("id") Integer id);

    /**
     * 更新收藏次数
     */
    int incrementFavoriteCount(@Param("id") Integer id);

    /**
     * 减少收藏次数
     */
    int decrementFavoriteCount(@Param("id") Integer id);

    /**
     * 更新评分
     */
    int updateRating(@Param("id") Integer id,
                     @Param("rating") BigDecimal rating);

    /**
     * 更新状态
     */
    int updateStatus(@Param("id") Integer id,
                     @Param("status") Integer status);

    /**
     * 批量更新状态
     */
    int batchUpdateStatus(@Param("ids") List<Integer> ids,
                          @Param("status") Integer status);

    /**
     * 统计各分类数量
     */
    List<CategoryStat> countByCategory();

    /**
     * 统计热门区域
     */
    List<HotAreaStat> countByHotArea(@Param("limit") Integer limit);

    /**
     * 内部类：分类统计
     */
    class CategoryStat {
        private Integer categoryId;
        private String categoryName;
        private Integer count;
        // getters and setters
        public Integer getCategoryId() { return categoryId; }
        public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }

    /**
     * 内部类：热门区域统计
     */
    class HotAreaStat {
        private String areaName;
        private Integer count;
        // getters and setters
        public String getAreaName() { return areaName; }
        public void setAreaName(String areaName) { this.areaName = areaName; }
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }
}
