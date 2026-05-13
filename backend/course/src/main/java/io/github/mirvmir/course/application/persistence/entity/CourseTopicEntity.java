package io.github.mirvmir.course.application.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@Getter
@Entity
@Table(name = "course_topic")
public class CourseTopicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private CourseEntity courseEntity;

    @Column(name = "topic_id", nullable = false)
    private Long topicId;

    public CourseTopicEntity(CourseEntity courseEntity,
                             Long topicId) {
        this.courseEntity = courseEntity;
        this.topicId = topicId;
    }
}
