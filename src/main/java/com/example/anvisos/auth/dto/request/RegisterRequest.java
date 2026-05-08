package com.example.anvisos.auth.dto.request;

import com.example.anvisos.model.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    @NotBlank
    @jakarta.validation.constraints.Pattern(
        regexp = "^[a-zA-ZÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠàáâãèéêìíòóôõùúăđĩũơƯĂẠẢẤẦẨẪẬẮẰẲẴẶẸẺẼỀỀỂưăạảấầẩẫậắằẳẵặẹẻẽềềểỄỆỈỊỌỎỐỒỔỖỘỚỜỞỠỢỤỦỨỪễệỉịọỏốồổỗộớờởỡợụủứừỬỮỰỲỴÝỶỸửữựỳỵýỷỹ\\s]+$",
        message = "Họ tên không được chứa ký tự đặc biệt"
    )
    private String fullName;

    @NotBlank
    @jakarta.validation.constraints.Pattern(
        regexp = "^(03|05|07|08|09)[0-9]{8}$",
        message = "Số điện thoại không hợp lệ tại Việt Nam"
    )
    private String phone;

    @NotBlank
    @jakarta.validation.constraints.Pattern(
        regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$",
        message = "Định dạng email không hợp lệ"
    )
    private String email;

    @NotBlank
    private String password;

    private UserRole role;

    private String bloodType;
    private Integer birthYear;
}
