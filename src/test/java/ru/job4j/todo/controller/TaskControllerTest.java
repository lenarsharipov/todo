package ru.job4j.todo.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ui.ConcurrentModel;
import ru.job4j.todo.model.Category;
import ru.job4j.todo.model.Priority;
import ru.job4j.todo.model.Task;
import ru.job4j.todo.model.User;
import ru.job4j.todo.service.CategoryService;
import ru.job4j.todo.service.PriorityService;
import ru.job4j.todo.service.TaskService;
import ru.job4j.todo.util.Attribute;
import ru.job4j.todo.util.Message;
import ru.job4j.todo.util.Page;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.MIN;
import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TaskControllerTest {
    private TaskController taskController;
    private TaskService taskService;
    private PriorityService priorityService;
    private CategoryService categoryService;
    private User admin;
    private Priority urgently;
    private Priority normal;
    private List<Task> tasks;
    private List<Category> categories;

    /**
     * Инициализировать taskService, taskController, списка задач tasks перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        taskService = mock(TaskService.class);
        priorityService = mock(PriorityService.class);
        categoryService = mock(CategoryService.class);
        taskController = new TaskController(taskService, priorityService, categoryService);

        var oldDate = LocalDateTime.of(
                LocalDate.of(MIN.getYear(), 1, 1),
                LocalTime.of(0, 0, 0));

        admin = new User(1, "admin", "admin", "123456", "UTC");

        categories = new ArrayList<>();
        categories.add(new Category(1, "Job"));
        categories.add(new Category(2, "Hobby"));
        categories.add(new Category(3, "Sport"));

        urgently = new Priority();
        urgently.setId(1);
        urgently.setName("urgently");
        urgently.setPosition(1);
        urgently.setPosition(1);
        normal = new Priority();
        normal.setId(2);
        normal.setName("normal");
        normal.setPosition(2);

        tasks = new ArrayList<>();
        tasks.add(new Task(1, "task1", oldDate, false, admin, urgently, categories));
        tasks.add(new Task(2, "task2", now(), true, admin, normal, categories));
        tasks.add(new Task(3, "task3", now(), false, admin, urgently, categories));
    }

    /**
     * Mock test getAll().
     * Вернуть страницу со всеми задачами сортированными по возрастанию по ID.
     */
    @Test
    void whenRequestTaskListPageThenGetPageWithTasksOrderedByIdAsc() {
        var expectedTasks = tasks;
        var request = mock(HttpServletRequest.class);
        when(request.getAttribute("user")).thenReturn(admin);
        when(taskService.findAll()).thenReturn(expectedTasks);

        var model = new ConcurrentModel();
        var view = taskController.getAll(model, request);
        var actualTasks = model.getAttribute("tasks");

        assertThat(view).isEqualTo("tasks/list");
        assertThat(actualTasks).isEqualTo(expectedTasks);
    }

    /**
     * Mock-test getCompleted().
     * Вернуть страницу со всеми выполненными задачами сортированными по возрастанию по ID.
     */
    @Test
    void whenRequestCompletedTaskListThenGetPageWithTasksOrderedByIdAsc() {
        var expectedTasks = List.of(tasks.get(1));
        var request = mock(HttpServletRequest.class);
        when(request.getAttribute("user")).thenReturn(admin);
        when(taskService.findAllCompleted(true)).thenReturn(expectedTasks);

        var model = new ConcurrentModel();
        var view = taskController.getCompleted(model, request);
        var actualTasks = model.getAttribute("tasks");

        assertThat(view).isEqualTo("tasks/completed");
        assertThat(actualTasks).isEqualTo(expectedTasks);
    }

    /**
     * Mock-test getNew().
     * Вернуть страницу со всеми новыми задачами сортированными по возрастанию по ID.
     */
    @Test
    void whenRequestNewTaskListThenGetPageWithNewTasksOrderedByIdAsc() {
        var expectedTasks = List.of(tasks.get(1), tasks.get(2));
        var request = mock(HttpServletRequest.class);
        when(request.getAttribute("user")).thenReturn(admin);
        when(taskService.findAllNew()).thenReturn(expectedTasks);

        var model = new ConcurrentModel();
        var view = taskController.getNew(model, request);
        var actualTasks = model.getAttribute("tasks");

        assertThat(view).isEqualTo("tasks/new");
        assertThat(actualTasks).isEqualTo(expectedTasks);
    }

    /**
     * Mock-test getCreationPage().
     * Вернуть страницу создания новой задачи.
     */
    @Test
    void whenRequestTaskCreationLPageThenGetIt() {
        var expectedView = "tasks/create";

        var model = new ConcurrentModel();
        var view = taskController.getCreationPage(model);

        assertThat(view).isEqualTo(expectedView);
    }

    /**
     * Mock-test create().
     * Вернуть созданную задачу и перейти на страницу задач.
     */
    @Test
    void whenSaveTaskThenSameDataAndRedirectToTasksPage() {
        var request = mock(HttpServletRequest.class);
        var task = new Task(1, "task1", LocalDateTime.now(), false, admin, urgently, categories);
        var taskArgumentCaptor = ArgumentCaptor.forClass(Task.class);
        when(request.getParameterValues(any())).thenReturn(new String[]{"1", "2", "3"});
        when(taskService.save(taskArgumentCaptor.capture())).thenReturn(Optional.of(task));

        var model = new ConcurrentModel();
        var view = taskController.create(task, model, request);
        var actualTask = taskArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/tasks");
        assertThat(actualTask).isEqualTo(task);
    }

    /**
     * Mock-test getById().
     * Вернуть страницу искомой задачи.
     */
    @Test
    void whenRequestTaskByIdThenGetTaskPage() {
        var expectedTask = Optional.of(tasks.get(0));
        var request = mock(HttpServletRequest.class);
        when(request.getAttribute("user")).thenReturn(admin);
        when(taskService.findById(1)).thenReturn(expectedTask);

        var model = new ConcurrentModel();
        var view = taskController.getById(1, model, request);
        var actualTask = model.getAttribute("task");

        assertThat(view).isEqualTo("tasks/one");
        assertThat(actualTask).isEqualTo(expectedTask.get());
    }

    /**
     * Mock-test getById().
     * Вернуть страницу об ошибке, если задача по ID не найдена.
     */
    @Test
    void whenRequestTaskByIllegalIdThenGetErrorPageWithMessage() {
        var expectedException = new RuntimeException(Message.TASK_NOT_FOUND);
        var request = mock(HttpServletRequest.class);
        var id = 0;
        when(taskService.findById(id)).thenReturn(Optional.empty());

        var model = new ConcurrentModel();
        var view = taskController.getById(id, model, request);
        var actualExceptionMessage = model.getAttribute(Attribute.MESSAGE);

        assertThat(view).isEqualTo(Page.ERRORS_404);
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    /**
     * Mock-test delete().
     * Удалить задачу по ID и вернуть страницу tasks.
     */
    @Test
    void whenDeleteTaskByIdThenGetTrueAndTasksPage() {
        var isDeleted = true;
        var id = 1;
        when(taskService.delete(id)).thenReturn(isDeleted);

        var model = new ConcurrentModel();
        var view = taskController.delete(id, model);

        assertThat(view).isEqualTo(Page.REDIRECT_TASKS);
    }

    /**
     * Mock-test delete().
     * Вернуть страницу с описанием ошибки, при ошибке удаления задачи.
     */
    @Test
    void whenDeleteByIllegalIdThenGetFalseAndErrorPageWithMessage() {
        var isDeleted = false;
        var expectedException = new RuntimeException(Message.TASK_NOT_FOUND);
        var id = 0;
        when(taskService.delete(id)).thenReturn(isDeleted);

        var model = new ConcurrentModel();
        var view = taskController.delete(id, model);
        var actualExceptionMessage = model.getAttribute(Attribute.MESSAGE);

        assertThat(view).isEqualTo(Page.ERRORS_404);
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    /**
     * Mock-test getEditPage().
     * Вернуть страницу редактирования задачи.
     */
    @Test
    void whenRequestEditPageThenGetPage() {
        var taskOptional = Optional.of(tasks.get(0));
        var id = 1;
        when(taskService.findById(id)).thenReturn(taskOptional);

        var model = new ConcurrentModel();
        var view = taskController.getEditPage(id, model);

        assertThat(view).isEqualTo(Page.TASKS_EDIT);
    }

    /**
     * Mock-test getEditPage().
     * Вернуть страницу с указанием ошибки, в случае, если искомая задача не найдена.
     */
    @Test
    void whenRequestEditPageOfNotExistingTaskThenGetErrorPageWithMessage() {
        var expectedErrorMessage = Message.TASK_NOT_FOUND;
        Optional<Task> expectedOptional = Optional.empty();
        var id = 0;
        when(taskService.findById(0)).thenReturn(expectedOptional);

        var model = new ConcurrentModel();
        var view = taskController.getEditPage(id, model);
        var actualErrorMessage = model.getAttribute(Attribute.MESSAGE);

        assertThat(view).isEqualTo(Page.ERRORS_404);
        assertThat(actualErrorMessage).isEqualTo(expectedErrorMessage);
    }

    /**
     * Mock-test update().
     * Вернуть страницу tasks, при успешном обновлении задачи.
     */
    @Test
    void whenUpdateTaskThenGetTasksPage() {
        var updatedTask = tasks.get(0);
        var request = mock(HttpServletRequest.class);
        updatedTask.setDescription("desc UPDATED");
        var taskArgumentCaptor = ArgumentCaptor.forClass(Task.class);
        when(request.getParameterValues(any())).thenReturn(new String[]{"1", "2", "3"});
        when(taskService.update(taskArgumentCaptor.capture())).thenReturn(true);

        var model = new ConcurrentModel();
        var view = taskController.update(updatedTask, model, request);
        var actualTask = taskArgumentCaptor.getValue();

        assertThat(view).isEqualTo(Page.REDIRECT_TASKS);
        assertThat(actualTask).isEqualTo(updatedTask);
    }

    /**
     * Mock-test update().
     * Вернуть страницу об ошибке, в случае, если задача не обновлена.
     * */
    @Test
    void whenUpdateNotExistingTaskThenGetErrorPageWithMessage() {
        var expectedErrorMessage = Message.TASK_NOT_UPDATED;
        var request = mock(HttpServletRequest.class);
        var taskArgumentCaptor = ArgumentCaptor.forClass(Task.class);
        when(request.getParameterValues(any())).thenReturn(new String[]{"1", "2", "3"});
        when(taskService.update(taskArgumentCaptor.capture())).thenReturn(false);

        var model = new ConcurrentModel();
        var view = taskController.update(new Task(), model, request);
        var actualErrorMessage = model.getAttribute(Attribute.MESSAGE);

        assertThat(view).isEqualTo(Page.ERRORS_404);
        assertThat(actualErrorMessage).isEqualTo(expectedErrorMessage);
    }

    /**
     * Mock-test updateStatus().
     * Вернуть страницу tasks, при успешном обновлении статуса задачи с "В процессе" на "Выполнено.
     */
    @Test
    void whenUpdateTaskStatusThenGetTasksPage() {
        var updatedTask = tasks.get(0);
        var id = updatedTask.getId();
        updatedTask.setDone(true);
        when(taskService.updateStatus(id)).thenReturn(true);

        var model = new ConcurrentModel();
        var view = taskController.updateStatus(id, model);

        assertThat(view).isEqualTo(Page.REDIRECT_TASKS);
    }

    /**
     * Mock-test update().
     * Вернуть страницу об ошибке, в случае, если статус задачи не обновлен.
     */
    @Test
    void whenUpdateNotExistingTaskStatusThenGetErrorPageWithMessage1() {
        var expectedErrorMessage = Message.TASK_NOT_UPDATED;
        var id = 0;
        when(taskService.updateStatus(id)).thenReturn(false);

        var model = new ConcurrentModel();
        var view = taskController.updateStatus(id, model);
        var actualErrorMessage = model.getAttribute(Attribute.MESSAGE);

        assertThat(view).isEqualTo(Page.ERRORS_404);
        assertThat(actualErrorMessage).isEqualTo(expectedErrorMessage);
    }

}