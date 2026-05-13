package io.github.mirvmir.course.domain;

import io.github.mirvmir.course.domain.content.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class LessonBlockHashCalculator {

    private LessonBlockHashCalculator() {
    }

    public static String calculate(LessonContent content) {
        String normalizedContent = normalize(content);
        return sha256(normalizedContent);
    }

    private static String normalize(LessonContent content) {
        if (content == null) {
            return "";
        }

        return switch (content.type()) {
            case HTML -> {
                LessonHtml html = (LessonHtml) content;

                yield """
                        type=%s
                        html=%s
                        """.formatted(
                        LessonContentType.HTML.name(),
                        normalizeHtml(html.getHtml())
                );
            }
            case FILE -> {
                LessonFile file = (LessonFile) content;

                yield """
                        type=%s
                        fileAssetId=%s
                        """.formatted(
                        LessonContentType.FILE.name(),
                        file.getFileAssetId()
                );
            }
            case VIDEO -> {
                LessonVideo video = (LessonVideo) content;

                yield """
                        type=%s
                        videoAssetId=%s
                        """.formatted(
                        LessonContentType.VIDEO.name(),
                        video.getVideoAssetId()
                );
            }
        };
    }

    private static String normalizeHtml(String html) {
        if (html == null) {
            return "";
        }

        return html
                .trim()
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replaceAll("[ \\t]+", " ");
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder result = new StringBuilder();

            for (byte b : hash) {
                result.append(String.format("%02x", b));
            }

            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is not available",
                    e
            );
        }
    }
}