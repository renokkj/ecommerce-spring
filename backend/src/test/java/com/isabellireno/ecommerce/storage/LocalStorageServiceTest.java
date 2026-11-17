package com.isabellireno.ecommerce.storage;

import com.isabellireno.ecommerce.common.BusinessException;
import com.isabellireno.ecommerce.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalStorageServiceTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};

    @TempDir
    Path tempDir;

    private LocalStorageService storage;

    @BeforeEach
    void setUp() {
        AppProperties props = new AppProperties("http://localhost:8080", null,
                null, new AppProperties.Storage(tempDir.toString()), null, null);
        storage = new LocalStorageService(props);
    }

    @Test
    void storesPngAndReturnsPublicUrl() {
        String url = storage.store(new MockMultipartFile("file", "foto.png", "image/png", PNG));

        assertThat(url).startsWith("http://localhost:8080/uploads/").endsWith(".png");
        String filename = url.substring(url.lastIndexOf('/') + 1);
        assertThat(Files.exists(tempDir.resolve(filename))).isTrue();
    }

    @Test
    void rejectsFileThatOnlyPretendsToBeAnImage() {
        MockMultipartFile fake = new MockMultipartFile("file", "virus.png", "image/png", "<script>".getBytes());

        assertThatThrownBy(() -> storage.store(fake))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Formato");
    }

    @Test
    void deleteIgnoresUrlsOutsideTheUploadFolder() {
        storage.delete("http://localhost:8080/uploads/../../etc/passwd");
        storage.delete("https://outro-site.com/imagem.png");
        // nenhum erro e nada fora da pasta é tocado
        assertThat(Files.exists(tempDir)).isTrue();
    }
}
