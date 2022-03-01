package com.cb.service;

import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.cb.model.FileMeta;
import com.cb.repository.FileMetaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class MetadataServiceImpl implements MetadataService {

    @Autowired
    private AmazonS3Service amazonS3Service;

    @Autowired
    private FileMetaRepository fileMetaRepository;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Override
    public void upload(MultipartFile file) throws IOException {

        if (file.isEmpty())
            throw new IllegalStateException("Cannot upload empty file");

        Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type", file.getContentType());
        metadata.put("Content-Length", String.valueOf(file.getSize()));

        String path = String.format("%s/%s", bucketName, UUID.randomUUID());
        String fileName = String.format("%s", file.getOriginalFilename());

        // Uploading file to s3
        PutObjectResult putObjectResult = amazonS3Service.upload(
                path, fileName, Optional.of(metadata), file.getInputStream());

        // Saving metadata to db
        fileMetaRepository.save(new FileMeta(fileName, path, putObjectResult.getMetadata().getVersionId()));

    }

    @Override
    public S3Object download(int id) {
        FileMeta fileMeta = fileMetaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException());
        return amazonS3Service.download(fileMeta.getFilePath(),fileMeta.getFileName());
    }

    @Override
    public List<FileMeta> list() {
        List<FileMeta> metas = new ArrayList<>();
        fileMetaRepository.findAll().forEach(metas::add);
        return metas;
    }
}
