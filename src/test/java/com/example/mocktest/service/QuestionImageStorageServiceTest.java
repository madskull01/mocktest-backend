package com.example.mocktest.service;

import com.example.mocktest.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestionImageStorageServiceTest {

    @TempDir
    Path tempDirectory;

    @Test
    void storesValidImageAndDeletesIt() throws Exception {
        QuestionImageStorageService storage =
                new QuestionImageStorageService(tempDirectory.toString());
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "question.png",
                "image/png",
                createPng());

        String imageUrl = storage.store(image);
        Path storedImage = tempDirectory.resolve(
                imageUrl.substring("/uploads/question-images/".length()));

        assertTrue(Files.exists(storedImage));
        assertTrue(imageUrl.endsWith(".png"));

        storage.delete(imageUrl);

        assertFalse(Files.exists(storedImage));
    }

    @Test
    void rejectsNonImageUploads() {
        QuestionImageStorageService storage =
                new QuestionImageStorageService(tempDirectory.toString());
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "question.png",
                "image/png",
                "not an image".getBytes());

        assertThrows(BadRequestException.class, () -> storage.store(image));
    }

    private byte[] createPng() throws Exception {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }
}
