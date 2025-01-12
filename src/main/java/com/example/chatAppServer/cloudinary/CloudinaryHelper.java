package com.example.chatAppServer.cloudinary;

import com.cloudinary.utils.ObjectUtils;
import com.example.chatAppServer.common.Common;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public class CloudinaryHelper {
    public static com.cloudinary.Cloudinary cloudinary;

    static {
        cloudinary = new com.cloudinary.Cloudinary(
                ObjectUtils.asMap(
                        Common.CLOUDINARY_NAME, Common.CLOUDINARY_NAME_VALUE,
                        Common.CLOUDINARY_API_KEY, Common.CLOUDINARY_API_KEY_VALUE,
                        Common.CLOUDINARY_API_SECRET, Common.CLOUDINARY_API_SECRET_VALUE
                )
        );
        System.out.println("SUCCESS GENERATE INSTANCE FOR CLOUDINARY");
    }

    public static String uploadAndGetFileUrl(MultipartFile multipartFile){
        try {
            Map uploadResult = cloudinary.uploader().uploadLarge(multipartFile.getInputStream(), ObjectUtils.emptyMap());
            return  uploadResult.get("url").toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
