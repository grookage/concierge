package com.grookage.concierge.core.engine;

import com.grookage.concierge.core.managers.ProcessorFactory;
import com.grookage.concierge.core.utils.ContextUtils;
import com.grookage.concierge.models.config.ConfigDetails;
import com.grookage.concierge.models.config.ConfigEvent;
import com.grookage.concierge.models.config.ConfigHistoryItem;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public abstract class ConciergeProcessor {

    public abstract ConfigEvent name();

    @SneakyThrows
    public void addHistory(ConciergeContext context, ConfigDetails configDetails) {
        final var userName = ContextUtils.getUser(context);
        final var email = ContextUtils.getEmail(context);
        final var userId = ContextUtils.getUserId(context);
        final var configHistoryItem = ConfigHistoryItem.builder()
                .configUpdaterId(userId)
                .configUpdaterEmail(email)
                .configUpdaterName(userName)
                .timestamp(System.currentTimeMillis())
                .configEvent(name())
                .build();
        configDetails.addHistory(configHistoryItem);
    }

    public abstract void process(ConciergeContext context);

    public void fire(ConciergeContext context, ProcessorFactory processorFactory) {
        process(context);
        final var configEvent = name();
        if (null != processorFactory) {
            final var postProcessor = processorFactory.getProcessor(configEvent).orElse(null);
            if (null != postProcessor) {
                postProcessor.process(context);
            }
        }
    }
}
