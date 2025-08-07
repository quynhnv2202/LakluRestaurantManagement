package com.laklu.pos.auth;

import com.laklu.pos.entities.Profile;
import com.laklu.pos.enums.EmploymentStatus;
import com.laklu.pos.exceptions.InvalidCredentialsException;
import com.laklu.pos.exceptions.UserInactiveException;
import com.laklu.pos.services.ProfileService;
import com.laklu.pos.valueObjects.UserCredentials;
import com.laklu.pos.valueObjects.UserPrincipal;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtGuard jwtGuard;
    private final ProfileService profileService;

    public boolean attempt(UserCredentials credentials) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(credentials.getUsername(), credentials.getPassword())
            );
            return authentication != null && authentication.isAuthenticated();
        } catch (Exception e) {
            throw new InvalidCredentialsException();
        }
    }

    public String login(UserCredentials credentials) {
        if (this.attempt(credentials)) {
            // Sau khi xác thực thành công, kiểm tra trạng thái làm việc của người dùng
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(credentials.getUsername(), credentials.getPassword())
            );
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            int userId = userPrincipal.getPersitentUser().getId();
            
            Optional<Profile> profileOpt = profileService.findByUserId(userId);
            if (profileOpt.isPresent()) {
                Profile profile = profileOpt.get();
                // Chỉ cho phép người dùng có trạng thái WORKING đăng nhập
                if (profile.getEmploymentStatus() != EmploymentStatus.WORKING) {
                    throw new UserInactiveException("Tài khoản không trong trạng thái làm việc");
                }
            }
            
            return this.jwtGuard.issueToken(credentials);
        }
        return null;
    }
}
