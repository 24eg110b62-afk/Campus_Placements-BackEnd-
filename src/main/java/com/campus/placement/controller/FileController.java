package com.campus.placement.controller;

import com.campus.placement.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Serves uploaded resumes to logged-in users. The frontend fetches them with the JWT and opens a blob. */
@RestController
@RequestMapping("/api/files")
public class FileController {
    private final Path dir;

    public FileController(@Value("${app.upload-dir}") String uploadDir) {
        this.dir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @GetMapping("/{name:.+}")
    public ResponseEntity<Resource> get(@PathVariable String name) {
        Path file = dir.resolve(name).normalize();
        if (!file.startsWith(dir) || !Files.exists(file))
            throw new ApiException(HttpStatus.NOT_FOUND, "File not found");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + name + "\"")
                .body(new PathResource(file));
    }
}
