package com.laklu.pos.entities;

import com.laklu.pos.controllers.ActivityLogListener;
import com.laklu.pos.valueObjects.UserPrincipal;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
//@EntityListeners(ActivityLogListener.class)
public class User implements Identifiable<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Setter
    @Column(nullable = false, unique = true)
    private String username;

    @Setter
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Setter
    @ManyToOne
    @JoinColumn(name = "salary_rate_id")
    private SalaryRate salaryRate;

    @Override
    public Integer getId() {
        return id;
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Setter
    private Set<Role> roles;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ScheduleUser> scheduleUsers = new ArrayList<>();

    public SalaryRate getSalaryRate() {
        return salaryRate != null ? salaryRate : null;
    }

    public void setPassword(String password, PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(password);
    }

    public UserPrincipal toUserPrincipal() {
        return new UserPrincipal(this);
    }
}
