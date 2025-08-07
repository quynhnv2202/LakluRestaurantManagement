package com.laklu.pos.dataObjects.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewTable {
    @NotBlank(message = "Không được để trống tên bàn") // Không được null hoặc rỗng
    @Size(max = 50, message = "Tên bàn không được quá 50 kí tự") // Giới hạn độ dài
    String tableNumber;

    @NotNull(message = "Không được bỏ trống số lượng khách") // Không được null
    @Min(value = 1, message = "Số lượng khách một bàn tối thiểu là 1") // Giá trị tối thiểu là 1
    @Max(value = 20, message = "Số lượng khách một bàn tối đa là 20") // Giới hạn số lượng tối đa
    Integer capacity;
}
