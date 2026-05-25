package com.example.campus.service;

import com.example.campus.service.impl.StatisticsServiceImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 排行榜定时任务服务
 */
@Service
public class RankingScheduleService {

    private final StatisticsServiceImpl statisticsService;

    public RankingScheduleService(StatisticsServiceImpl statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * 定时更新设施排名
     * 每小时执行一次（整点执行）
     * cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateRankings() {
        System.out.println("开始执行排行榜定时更新任务...");
        try {
            // 调用重载方法，传入true表示需要更新排名
            statisticsService.getTopFacilities(50, true);
            System.out.println("排行榜定时更新任务执行成功！");
        } catch (Exception e) {
            System.err.println("排行榜定时更新任务执行失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
}
