package com.example.chatAppServer.helper;

import com.example.chatAppServer.cloudinary.CloudinaryHelper;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class StringUtils {
    public static String convertListToString(List<String> imageUrls){
        if (Objects.isNull(imageUrls) || imageUrls.isEmpty()){
            return null;
        }
        StringBuilder result = new StringBuilder();
        Integer index = 0;
        for (String imageUrl : imageUrls){   // anh1;anh2;anh3
            result.append(imageUrl);
            if (index != imageUrls.size() - 1){
                result.append(";");
            }
            index ++;
        }
        return result.toString();
    }

    public static List<String> getListFromString(String imageUrls){
        if (Objects.isNull(imageUrls)) {
            return new ArrayList<>();
        }
        return Arrays.asList(imageUrls.split(";"));
    }

    public static List<String> getImageUrls(List<MultipartFile> multipartFiles) {
        if (Objects.isNull(multipartFiles) || multipartFiles.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            imageUrls.add(CloudinaryHelper.uploadAndGetFileUrl(multipartFile));
        }
        return imageUrls;
    }
}
