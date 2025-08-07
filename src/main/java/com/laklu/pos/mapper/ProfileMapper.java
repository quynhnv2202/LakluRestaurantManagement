package com.laklu.pos.mapper;

import com.laklu.pos.dataObjects.request.CreateProfileRequest;
import com.laklu.pos.dataObjects.request.UpdateProfileRequest;
import com.laklu.pos.dataObjects.response.ProfileResponse;
import com.laklu.pos.entities.Profile;
import com.laklu.pos.entities.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    Profile toProfile(CreateProfileRequest createProfileRequest);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProfileFromDto(UpdateProfileRequest updateProfileRequest, @MappingTarget Profile profile);
    
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "email", source = "user.email")
    ProfileResponse toProfileResponse(Profile profile);
}
