package me.yyb.open.shedule;


import me.yyb.open.biz.OpenService;
import me.yyb.open.domain.AuthorizationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author: yyb
 * @date: 17-9-7
 */
@Component
public class TimedTask {

    private static Logger logger = LoggerFactory.getLogger(TimedTask.class);

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    OpenService openService;

    private volatile LinkedBlockingQueue<AuthorizationInfo> queue = new LinkedBlockingQueue<>(300);

    private volatile boolean isEnd = false;

    @Scheduled(fixedDelay = 3600000L)
    public void refreshAccessToken() {
        // TODO 这里要取刷新 AuthorizationInfo 中的accessToken
        logger.info("-->> 开始刷新refreshAccessToken");
        isEnd = false;
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        if (queue.size() == 0 && isEnd) {
                            break;
                        }
                        AuthorizationInfo info = queue.poll(5, TimeUnit.SECONDS);
                        if (info != null) {
                            openService.refresh(info);
                        }
                    } catch (InterruptedException e) {
                        logger.error("刷新token错误:", e);
                    }
                }
                logger.info("更新完毕:" + Thread.currentThread().getName());
            }, "doRefresh-" + i).start();
        }

        new Thread(() -> {
            try {
                List<AuthorizationInfo> list = mongoTemplate.findAll(AuthorizationInfo.class);
                logger.info("-->> 更新token:" + list.size());
                for (AuthorizationInfo authorizationInfo : list) {
                    queue.put(authorizationInfo);
                }
            } catch (InterruptedException e) {
                logger.error("put认证信息错误:", e);
            } finally {
                isEnd = true;
            }
        }, "put-authorizationInfo").start();
    }

}
