package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ConcurrentModel;
import ru.job4j.dreamjob.model.User;
import ru.job4j.dreamjob.service.UserService;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserControllerTest {

    private UserService userService;

    private UserController userController;

    private HttpServletRequest request;

    @BeforeEach
    public void initServices() {
        userService = mock(UserService.class);
        userController = new UserController(userService);
        request = new MockHttpServletRequest();
    }

    @Test
    void whenRequestRegistrationPageThenGetRegistrationPage() {
        assertThat(userController.getRegistrationPage()).isEqualTo("users/register");
    }

    @Test
    void whenPostUserThenSameDataAndRedirectToVacanciesPage() {
        var user = new User(1, "email", "name", "password");
        var userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        when(userService.save(userArgumentCaptor.capture())).thenReturn(Optional.of(user));

        var model = new ConcurrentModel();
        var view = userController.register(user, model);
        var actualUser = userArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/vacancies");
        assertThat(actualUser).isEqualTo(user);
    }

    @Test
    void whenPostUserWithEmailWhichExistsThenGetErrorPageWithMessage() {
        var expectedException = new RuntimeException("Пользователь с такой почтой уже существует");
        when(userService.save(any())).thenReturn(Optional.empty());

        var model = new ConcurrentModel();
        var view = userController.register(new User(), model);
        var actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    @Test
    void whenRequestLoginPageThenGetLoginPage() {
        assertThat(userController.getLoginPage()).isEqualTo("users/login");
    }

    @Test
    void whenPostLoginUserThenRedirectToVacanciesPage() {
        var user = new User(1, "email", "name", "password");
        when(userService.findByEmailAndPassword(any(), any())).thenReturn(Optional.of(user));

        var model = new ConcurrentModel();
        var view = userController.loginUser(user, model, request);
        var actualUser = request.getSession().getAttribute("user");

        assertThat(view).isEqualTo("redirect:/vacancies");
        assertThat(actualUser).isEqualTo(user);
    }

    @Test
    void whenPostLoginUserWithWrongDatesThenRedirectToLoginPageWithError() {
        var expectedErrorMessage = "Почта или пароль введены неверно";
        when(userService.findByEmailAndPassword(any(), any())).thenReturn(Optional.empty());

        var model = new ConcurrentModel();
        var view = userController.loginUser(new User(), model, request);
        var actualErrorMessage = model.getAttribute("error");

        assertThat(view).isEqualTo("users/login");
        assertThat(actualErrorMessage).isEqualTo(expectedErrorMessage);
    }

    @Test
    void whenGetLogoutThenRedirectToLoginPage() {
        assertThat(userController.logout(request.getSession())).isEqualTo("redirect:/users/login");
    }
}