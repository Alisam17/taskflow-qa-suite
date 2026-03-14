package com.taskflow.app.controller;

import com.taskflow.app.model.Task;
import com.taskflow.app.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class UiController {

    private final TaskService taskService;

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("tasks", taskService.getAllTasks());
        model.addAttribute("stats", taskService.getTaskStats());
        model.addAttribute("newTask", new Task());
        return "dashboard";
    }

    @PostMapping("/tasks/create")
    public String createTask(@ModelAttribute Task task) {
        taskService.createTask(task);
        return "redirect:/";
    }

    @PostMapping("/tasks/{id}/delete")
    public String deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return "redirect:/";
    }
}
