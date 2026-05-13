package io.github.mirvmir.media.application.service.implementation;

import io.github.mirvmir.course.api.CourseApi;
import io.github.mirvmir.enrollment.api.EnrollmentApi;
import io.github.mirvmir.identity.api.IdentityApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DefaultVideoAccessServiceTest {

    private IdentityApi identityApi;
    private CourseApi courseApi;
    private EnrollmentApi enrollmentApi;

    private DefaultVideoAccessService service;

    @BeforeEach
    void setUp() {
        identityApi = mock(IdentityApi.class);
        courseApi = mock(CourseApi.class);
        enrollmentApi = mock(EnrollmentApi.class);

        service = new DefaultVideoAccessService(
                identityApi,
                courseApi,
                enrollmentApi
        );
    }

    @Test
    void checkCurrentUserCanWatch_whenAdmin_shouldAllow() {
        when(identityApi.getCurrentUserId()).thenReturn(2L);
        when(identityApi.hasRole("ADMIN")).thenReturn(true);

        service.checkCurrentUserCanWatch(10L);

        verify(courseApi, never()).canTeacherAccessVideo(anyLong(), anyLong());
        verify(enrollmentApi, never()).canUserAccessVideo(anyLong(), anyLong());
    }

    @Test
    void checkCurrentUserCanWatch_whenTeacherCanAccessVideo_shouldAllow() {
        when(identityApi.getCurrentUserId()).thenReturn(2L);
        when(identityApi.hasRole("ADMIN")).thenReturn(false);
        when(courseApi.canTeacherAccessVideo(2L, 10L)).thenReturn(true);

        service.checkCurrentUserCanWatch(10L);

        verify(courseApi).canTeacherAccessVideo(2L, 10L);
        verify(enrollmentApi, never()).canUserAccessVideo(2L, 10L);
    }

    @Test
    void checkCurrentUserCanWatch_whenStudentCanAccessVideo_shouldAllow() {
        when(identityApi.getCurrentUserId()).thenReturn(2L);
        when(identityApi.hasRole("ADMIN")).thenReturn(false);
        when(courseApi.canTeacherAccessVideo(2L, 10L)).thenReturn(false);
        when(enrollmentApi.canUserAccessVideo(2L, 10L)).thenReturn(true);

        service.checkCurrentUserCanWatch(10L);

        verify(courseApi).canTeacherAccessVideo(2L, 10L);
        verify(enrollmentApi).canUserAccessVideo(2L, 10L);
    }

    @Test
    void checkCurrentUserCanWatch_whenUserCannotAccessVideo_shouldThrowForbidden() {
        when(identityApi.getCurrentUserId()).thenReturn(2L);
        when(identityApi.hasRole("ADMIN")).thenReturn(false);
        when(courseApi.canTeacherAccessVideo(2L, 10L)).thenReturn(false);
        when(enrollmentApi.canUserAccessVideo(2L, 10L)).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.checkCurrentUserCanWatch(10L)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }
}
