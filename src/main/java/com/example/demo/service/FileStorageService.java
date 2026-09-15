package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${app.upload.dir:uploads/comprobantes}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo crear el directorio donde se almacenarán los comprobantes subidos.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // Generar un nombre único para evitar colisiones
        String fileName = UUID.randomUUID().toString() + fileExtension.toLowerCase();

        try {
            if (fileName.contains("..")) {
                throw new RuntimeException("El nombre del archivo contiene caracteres inválidos: " + fileName);
            }

            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Retornar ruta pública relativa para el frontend
            return "/uploads/comprobantes/" + fileName;
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo almacenar el archivo " + fileName + ". Intente nuevamente.", ex);
        }
    }
}
