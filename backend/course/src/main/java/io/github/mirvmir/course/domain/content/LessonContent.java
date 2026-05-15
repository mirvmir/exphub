package io.github.mirvmir.course.domain.content;

public interface LessonContent {
    LessonContent copyToDraft();
    LessonContentType type();
}