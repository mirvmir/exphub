package io.github.mirvmir.media.application.service.implementation;

import io.github.mirvmir.course.api.CourseApi;
import io.github.mirvmir.enrollment.api.EnrollmentApi;
import io.github.mirvmir.identity.api.IdentityApi;
import io.github.mirvmir.profile.api.ProfileApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DefaultMediaFileAccessServiceTest {

    private IdentityApi identityApi;
    private EnrollmentApi enrollmentApi;
    private ProfileApi profileApi;
    private CourseApi courseApi;

    private DefaultMediaFileAccessService service;

    @BeforeEach
    void setUp() {
        identityApi = mock(IdentityApi.class);
        enrollmentApi = mock(EnrollmentApi.class);
        profileApi = mock(ProfileApi.class);
        courseApi = mock(CourseApi.class);

        service = new DefaultMediaFileAccessService(
                identityApi,
                enrollmentApi,
                profileApi,
                courseApi
        );
    }

    @Test
    void checkCurrentUserCanAccess_whenAdmin_shouldAllow() {
        when(identityApi.getCurrentUserId()).thenReturn(2L);
        when(identityApi.hasRole("ADMIN")).thenReturn(true);

        service.checkCurrentUserCanAccess(1L);

        verify(courseApi, never()).canTeacherAccessFile(anyLong(), anyLong());
        verify(enrollmentApi, never()).canUserAccessFile(anyLong(), anyLong());
    }

    @Test
    void checkCurrentUserCanAccess_whenTeacherCanAccessFile_shouldAllow() {
        when(identityApi.getCurrentUserId()).thenReturn(2L);
        when(identityApi.hasRole("ADMIN")).thenReturn(false);
        when(courseApi.canTeacherAccessFile(2L, 1L)).thenReturn(true);

        service.checkCurrentUserCanAccess(1L);

        verify(courseApi).canTeacherAccessFile(2L, 1L);
        verify(enrollmentApi, never()).canUserAccessFile(2L, 1L);
    }

    @Test
    void checkCurrentUserCanAccess_whenStudentCanAccessFile_shouldAllow() {
        when(identityApi.getCurrentUserId()).thenReturn(2L);
        when(identityApi.hasRole("ADMIN")).thenReturn(false);
        when(courseApi.canTeacherAccessFile(2L, 1L)).thenReturn(false);
        when(enrollmentApi.canUserAccessFile(2L, 1L)).thenReturn(true);

        service.checkCurrentUserCanAccess(1L);

        verify(courseApi).canTeacherAccessFile(2L, 1L);
        verify(enrollmentApi).canUserAccessFile(2L, 1L);
    }

    @Test
    void checkCurrentUserCanAccess_whenUserCannotAccessFile_shouldThrowForbidden() {
        when(identityApi.getCurrentUserId()).thenReturn(2L);
        when(identityApi.hasRole("ADMIN")).thenReturn(false);
        when(courseApi.canTeacherAccessFile(2L, 1L)).thenReturn(false);
        when(enrollmentApi.canUserAccessFile(2L, 1L)).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.checkCurrentUserCanAccess(1L)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void checkCanBePublic_whenFileIsPublicAvatar_shouldAllow() {
        when(profileApi.existsPublicAvatar(1L)).thenReturn(true);

        service.checkCanBePublic(1L);

        verify(profileApi).existsPublicAvatar(1L);
    }

    @Test
    void checkCanBePublic_whenFileIsNotPublicAvatar_shouldThrowForbidden() {
        when(profileApi.existsPublicAvatar(1L)).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.checkCanBePublic(1L)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }
}
