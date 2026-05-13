package io.github.mirvmir.media.application.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Table(name = "video_asset")
public class VideoAssetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "poster_file_id")
    private Long posterFileId;

    @Column(name = "duration_seconds", nullable = false)
    private Integer durationSeconds;

    @Column(name = "storage_path", nullable = false)
    private String storagePath;
}