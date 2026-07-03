package com.platform.job.producer;

import com.platform.job.events.JobCreatedEvent;
import com.platform.messaging.Producer;

public interface JobProducer extends Producer<JobCreatedEvent> {
}
