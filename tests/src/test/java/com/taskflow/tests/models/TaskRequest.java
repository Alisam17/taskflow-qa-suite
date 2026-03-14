package com.taskflow.tests.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

// Request body for the tasks API. Factory methods at the bottom
// keep test code readable without repeating builder boilerplate.
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
