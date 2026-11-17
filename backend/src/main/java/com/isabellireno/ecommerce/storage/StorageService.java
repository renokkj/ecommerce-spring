package com.isabellireno.ecommerce.storage;

import org.springframework.web.multipart.MultipartFile;

/** Abstração do armazenamento de imagens; trocar por S3/Cloudinary não muda o resto do código. */
public interface StorageService {

    /** Salva o arquivo e devolve a URL pública. */
    String store(MultipartFile file);

    /** Remove o arquivo apontado pela URL, se ele for deste armazenamento. */
    void delete(String publicUrl);
}
