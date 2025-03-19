package com.grookage.concierge.core.engine;

import com.grookage.concierge.core.managers.EventProcessor;
import com.grookage.concierge.core.managers.ProcessorFactory;
import com.grookage.concierge.core.stubs.TestConfigUpdater;
import com.grookage.concierge.models.ConfigUpdater;
import com.grookage.concierge.models.processor.ProcessorKey;
import com.grookage.concierge.repository.ConciergeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.function.Supplier;

public abstract class AbstractProcessorTest {

    public static final ConfigUpdater configUpdater = new TestConfigUpdater();
    private static ConciergeProcessor conciergeProcessor;
    private static ConciergeRepository conciergeRepository;
    private static ProcessorFactory processorFactory = Mockito.mock(ProcessorFactory.class);
    private static EventProcessor eventProcessor;

    public ConfigUpdater getConfigUpdater() {
        return configUpdater;
    }

    public ConciergeRepository getConciergeRepository() {
        return conciergeRepository;
    }

    public ConciergeProcessor getConciergeProcessor() {
        return conciergeProcessor;
    }

    public Supplier<ProcessorFactory> getProcessorFactory() {
        return () -> processorFactory;
    }

    public abstract ConciergeProcessor createConciergeProcessor();

    @BeforeEach
    void setup() {
        conciergeRepository = Mockito.mock(ConciergeRepository.class);
        eventProcessor = Mockito.mock(EventProcessor.class);
        Mockito.when(processorFactory.getProcessor(Mockito.any(ProcessorKey.class)))
                .thenReturn(Optional.of(eventProcessor));
        conciergeProcessor = createConciergeProcessor();
    }

}
