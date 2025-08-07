package com.laklu.pos.dataObjects.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileReponse {
    String fullName;
    String genderLable;
    LocalDateTime dateOfBirth;
    String phoneNumber;
    String address;
    String avatar;
    String departmentLable;
    String employmentStatusLable;
    LocalDateTime hireDate;
    String bankAccount;
    String bankNumber;
    UserInfoResponse user;
}
