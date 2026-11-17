package com.isabellireno.ecommerce.storage;

import com.isabellireno.ecommerce.common.BusinessException;
import com.isabellireno.ecommerce.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

/**
 * Salva as imagens em disco (pasta UPLOAD_DIR) e as expõe em /uploads/**.
 * O tipo do arquivo é conferido pelos primeiros bytes, não só pelo Content-Type enviado pelo cliente.
 */
@Service
public class LocalStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageService.class);
    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp");

    private final Path root;
    private final String publicBaseUrl;

    public LocalStorageService(AppProperties properties) {
        this.root = Path.of(properties.storage().uploadDir()).toAbsolutePath().normalize();
        this.publicBaseUrl = properties.publicBaseUrl().replaceAll("/+$", "");
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível criar a pasta de uploads: " + root, e);
        }
    }

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Envie um arquivo de imagem.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BusinessException("A imagem deve ter no máximo 5 MB.");
        }
        String detectedType = detectImageType(file);
        String filename = UUID.randomUUID() + EXTENSIONS.get(detectedType);
        Path target = root.resolve(filename).normalize();
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao salvar a imagem.", e);
        }
        return publicBaseUrl + "/uploads/" + filename;
    }

    @Override
    public void delete(String publicUrl) {
        String prefix = publicBaseUrl + "/uploads/";
        if (publicUrl == null || !publicUrl.startsWith(prefix)) {
            return;
        }
        Path target = root.resolve(publicUrl.substring(prefix.length())).normalize();
        if (!target.startsWith(root)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("Não foi possível apagar {}", target, e);
        }
    }

    public Path getRoot() {
        return root;
    }

    static String detectImageType(MultipartFile file) {
        byte[] header = new byte[12];
        int read;
        try (InputStream in = file.getInputStream()) {
            read = in.readNBytes(header, 0, header.length);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (read >= 3 && (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (read >= 8 && (header[0] & 0xFF) == 0x89 && header[1] == 'P' && header[2] == 'N' && header[3] == 'G') {
            return "image/png";
        }
        if (read >= 12 && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') {
            return "image/webp";
        }
        throw new BusinessException("Formato não suportado. Use JPEG, PNG ou WebP.");
    }
}
