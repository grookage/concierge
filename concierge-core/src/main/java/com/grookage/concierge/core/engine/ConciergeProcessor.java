package com.grookage.concierge.core.engine;

import com.grookage.concierge.core.managers.ProcessorFactory;
import com.grookage.concierge.core.utils.ContextUtils;
import com.grookage.concierge.models.config.ConfigDetails;
import com.grookage.concierge.models.config.ConfigEvent;
import com.grookage.concierge.models.config.ConfigHistoryItem;
import com.grookage.concierge.models.exception.ConciergeCoreErrorCode;
import com.grookage.concierge.models.exception.ConciergeException;
import com.grookage.concierge.models.processor.ProcessorKey;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;


@Slf4j
@AllArgsConstructor
@SuperBuilder
public abstract class ConciergeProcessor {

    private final Supplier<ProcessorFactory> processorFactory;

    public abstract ConfigEvent name();

    @SneakyThrows
    public void addHistory(ConciergeContext context, ConfigDetails configDetails, String updateMessage) {
        final var userName = ContextUtils.getUser(context);
        final var email = ContextUtils.getEmail(context);
        final var userId = ContextUtils.getUserId(context);
        final var configHistoryItem = ConfigHistoryItem.builder()
                .configUpdaterId(userId)
                .configUpdaterEmail(email)
                .configUpdaterName(userName)
                .timestamp(System.currentTimeMillis())
                .configEvent(name())
                .message(updateMessage)
                .build();
        configDetails.addHistory(configHistoryItem);
    }

    public abstract void process(ConciergeContext context);

    @SneakyThrows
    public void fire(ConciergeContext context) {
        final var processorKey = context.getContext(ProcessorKey.class)
                .orElseThrow((Supplier<Throwable>) () -> ConciergeException.error(ConciergeCoreErrorCode.VALUE_NOT_FOUND));
        final var processor = null != processorFactory && null != processorFactory.get() ?
                processorFactory.get().getProcessor(processorKey).orElse(null) : null;

        if (null != processor) {
            processor.preProcess(processorKey, context);
        }
        process(context);

        if (null != processor) {
            processor.postProcess(processorKey, context);
        }
    }
}
