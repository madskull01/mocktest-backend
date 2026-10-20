package com.example.mocktest.service;

import com.example.mocktest.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;

@Service
public class QuestionImageStorageService {

    private static final long MAX_IMAGE_BYTES = 5 * 1024 * 1024;
    private static final long MAX_IMAGE_PIXELS = 40_000_000;
    private static final String IMAGE_URL_PREFIX = "/uploads/question-images/";

    private final Path imageDirectory;

    public QuestionImageStorageService(
            @Value("${app.uploads.question-images-dir:uploads/question-images}")
            String imageDirectory) {
        this.imageDirectory = Path.of(imageDirectory).toAbsolutePath().normalize();
    }

    public String store(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return null;
        }
        if (image.getSize() > MAX_IMAGE_BYTES) {
            throw new BadRequestException("Question image must be 5 MB or smaller");
        }

        try {
            byte[] content = image.getBytes();
            String extension = validateAndGetExtension(content);
            String fileName = UUID.randomUUID() + "." + extension;
            Files.createDirectories(imageDirectory);
            Files.write(
                    imageDirectory.resolve(fileName),
                    content,
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE);
            return IMAGE_URL_PREFIX + fileName;
        } catch (BadRequestException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not store the question image", exception);
        }
    }

    public void delete(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith(IMAGE_URL_PREFIX)) {
            return;
        }
        String fileName = imageUrl.substring(IMAGE_URL_PREFIX.length());
        Path imagePath = imageDirectory.resolve(fileName).normalize();
        if (!imagePath.getParent().equals(imageDirectory)) {
            throw new IllegalArgumentException("Invalid question image path");
        }
        try {
            Files.deleteIfExists(imagePath);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not remove the question image", exception);
        }
    }

    private String validateAndGetExtension(byte[] content) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(
                new ByteArrayInputStream(content))) {
            if (input == null) {
                throw new BadRequestException("Upload a valid PNG, JPEG, or GIF image");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new BadRequestException("Upload a valid PNG, JPEG, or GIF image");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width < 1 || height < 1
                        || (long) width * height > MAX_IMAGE_PIXELS) {
                    throw new BadRequestException("Question image dimensions are too large");
                }
                reader.read(0);
                return switch (reader.getFormatName().toLowerCase(Locale.ROOT)) {
                    case "png" -> "png";
                    case "jpeg", "jpg" -> "jpg";
                    case "gif" -> "gif";
                    default -> throw new BadRequestException(
                            "Upload a PNG, JPEG, or GIF image");
                };
            } finally {
                reader.dispose();
            }
        }
    }
}
