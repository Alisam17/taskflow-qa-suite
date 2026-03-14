package com.taskflow.tests.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * Request model for creating/updating tasks via the API.
 * Uses Builder pattern for clean test data setup.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskRequest {

    private String title;
    private String description;
    private String status;
    private String priority;

    // Convenience factory methods for readable test code
    public static TaskRequest validTask(String title) {
        return TaskRequest.builder()
                .title(title)
                .status("TODO")
                .priority("MEDIUM")
                .build();
    }

    public static TaskRequest highPriorityTask(String title) {
        return TaskRequest.builder()
                .title(title)
                .description("High priority task created by automation")
                .status("IN_PROGRESS")
                .priority("HIGH")
                .build();
    }

    public static TaskRequest criticalTask(String title) {
        return TaskRequest.builder()
                .title(title)
                .description("Critical task requiring immediate attention")
                .status("TODO")
                .priority("CRITICAL")
                .build();
    }
}
