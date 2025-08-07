package com.laklu.pos.services;

import com.laklu.pos.dataObjects.request.NewUser;
import com.laklu.pos.dataObjects.response.UserInfoResponse;
import com.laklu.pos.entities.Role;
import com.laklu.pos.entities.SalaryRate;
import com.laklu.pos.entities.User;
import com.laklu.pos.entities.Profile;
import com.laklu.pos.enums.EmploymentStatus;
import com.laklu.pos.exceptions.httpExceptions.NotFoundException;
import com.laklu.pos.repositories.RoleRepository;
import com.laklu.pos.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SalaryRateService salaryRateService;
    private final RoleRepository roleRepository;

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public Page<User> getAllPaginated(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User store(NewUser user) {
        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setPassword(user.getPassword(), passwordEncoder);
        newUser.setEmail(user.getEmail());
        Set<Role> roles = user.getRoleIds().stream()
                .map(roleId -> roleRepository.findRoleById(roleId)
                        .orElseThrow())
                .collect(Collectors.toSet());
        newUser.setRoles(roles);

        SalaryRate salaryRate = salaryRateService.findOrFail(user.getSalaryRateId());
        newUser.setSalaryRate(salaryRate);

        return userRepository.save(newUser);
    }

    // TODO: handle partial update
    public User update(User user) {
        return userRepository.save(user);
    }

    public Optional<User> findUserById(Integer id) {
        return userRepository.findById(id);
    }

    public User findOrFail(Integer id) {
        return this.findUserById(id).orElseThrow(NotFoundException::new);
    }

    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    public UserInfoResponse getUserInfoById(Integer userId) {
        User user = this.findOrFail(userId);
        UserInfoResponse userInfo = new UserInfoResponse();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());
        // Thiết lập các trường khác nếu cần
        return userInfo;
    }
    
    /**
     * Đổi mật khẩu người dùng khi người dùng tự đổi (yêu cầu xác thực mật khẩu cũ)
     */
    public boolean changePassword(Integer userId, String currentPassword, String newPassword) {
        User user = this.findOrFail(userId);
        if (passwordEncoder.matches(currentPassword, user.getPassword())) {
            user.setPassword(newPassword, passwordEncoder);
            userRepository.save(user);
            return true;
        }
        return false;
    }
    
    /**
     * Đổi mật khẩu người dùng bởi admin (không yêu cầu mật khẩu cũ)
     */
    public void adminChangePassword(Integer userId, String newPassword) {
        User user = this.findOrFail(userId);
        user.setPassword(newPassword, passwordEncoder);
        userRepository.save(user);
    }
}
