package io.queberry.que.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.queberry.que.anotation.AggregateReference;
import io.queberry.que.exception.QueueException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

@Entity(name = "que_employee")
@Table(name = "que_employee")
@Getter
@Setter
@ToString(exclude = "password")
@AllArgsConstructor
@NoArgsConstructor
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "EmployeeCache")
public class Employee extends AggregateRoot<Employee> implements UserDetails {
    @Column(unique = true)
//    @NotNull(message = "Username is required.")
//    @Pattern(regexp =  "^\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*(\\.\\w{2,3})+$", message = "Email is invalid")
    private String username;

    @JsonIgnore
//    @NotNull(message = "Password is required.")
//    @Size(min = 8, message = "Password should be minimum 8 characters!!")
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles = new HashSet<>();


    //    @NotNull(message = "First name is required")
//    @Pattern(regexp = "[A-Za-z0-9 ]*", message = "First name should contain only alphabets!!")
    private String firstname;

    //    @NotNull(message = "Last name is required")
//    @Pattern(regexp = "[A-Za-z0-9 ]*", message = "Last name should contain only alphabets!!")
    private String lastname;
    //
    //    @Pattern(regexp = "[A-Za-z0-9 ]*", message = "Middle name should contain only alphabets!!")
    private String middlename;

    @Column(name="counter_id")
    private String counter;

    @Column(columnDefinition = "bit default 1")
    private boolean active;

    @Column(columnDefinition = "bit default 1")
    private boolean walkIn;

    @Column(columnDefinition = "bit default 1")
    private boolean callByNumber;

    @Column(nullable = false, columnDefinition = "bit default 1")
    private boolean enableAutoCall;

    @Column(nullable = false, columnDefinition = "bit default 0")
    private boolean forceAutoCall;

    @Column(nullable = false, columnDefinition = "bit default 1")
    private boolean park;

    @Column(nullable = false, columnDefinition = "bit default 1")
    private boolean transferService;

    @Column(nullable = false, columnDefinition = "bit default 1")
    private boolean transferCounter;

    @Column(nullable = false, columnDefinition = "bit default 1")
    private boolean transferUser;

    @Column(nullable = false, columnDefinition = "bit default 1")
    private boolean break_btn;

    @Column(nullable = false, columnDefinition = "bit default 1")
    private boolean callAll; // both

    @Column(nullable = false, columnDefinition = "bit default 1")
    private boolean callNew;

    @Column(nullable = false, columnDefinition = "bit default 1")
    private boolean callTransfer;

    @Column(nullable = false, columnDefinition = "bit default 1")
    private boolean showServiceList;

    //   @ManyToMany(fetch = FetchType.EAGER)
    @ElementCollection(fetch=FetchType.EAGER)
    @Column(name="second")
    private Set<String> second = new TreeSet<>();

    // @OneToMany(fetch = FetchType.EAGER)
    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name="third")
    private Set<String> third = new TreeSet<>();

    // @ManyToMany(fetch = FetchType.EAGER)
    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name="fourth")
    private Set<String> fourth = new TreeSet<>();

    private String tenant;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name="branches")
    private Set<String> branches = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name="services")
    private Set<String> services = new HashSet<>();

    //    @ManyToOne
    //   @ElementCollection(fetch = FetchType.EAGER)
//    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "RegionCache")
    @Column(name="region")
    private String region;

    private String loggedCounter;
    private LocalDateTime loggedTime;
    private LocalDateTime passwordLastChanged;

    @Column(nullable = false, columnDefinition = "bit default 0")
    private boolean locked;

    private String passwordManagement;

    public Employee(String emp1) {
        super();
    }

    @JsonIgnore
    public Set<Role> getAuthorities(){
        return this.getRoles();
    }

    @Override
    public boolean isAccountNonExpired() {
        return !this.locked;
    }


    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    public Employee activate(){
        if (!this.active){
            this.active=true;
            registerEvent(EmployeeActivated.of(this));
        }
        else
            throw new QueueException("Employee is already active",HttpStatus.PRECONDITION_FAILED);
        return this;
    }

    public Employee deActivate(){
        if (this.active){
            this.active=false;
            registerEvent(EmployeeDeactivated.of(this));
        }
        else
            throw new QueueException("Employee is already deactivated",HttpStatus.PRECONDITION_FAILED);
        return this;
    }

    public Employee resetAdminPassword(String password){
        this.password = new BCryptPasswordEncoder().encode(password);
        return this;
    }

    public Employee resetPassword(String existing,String password){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (encoder.matches(existing, this.password)) {
            this.password = encoder.encode(password);
            return this;
        } else {
            throw new QueueException("Current or Old Password doesn't match",
                    HttpStatus.PRECONDITION_FAILED);
        }
    }

    public Employee(String username, String password,
                    String firstname,
                    String lastname,
                    String middlename,
                    String tenant,
                    Set<Role> roles, boolean active){
        this.username = username;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.middlename = middlename;
        this.tenant = tenant;
        this.roles = roles;
        this.active = active;
    }


    public void setId(String string) {
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    @RequiredArgsConstructor(staticName = "of")
    @NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
    public static class EmployeeActivated extends DomainEvent<Employee> {

        @AggregateReference
        final Employee employee;
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    @RequiredArgsConstructor(staticName = "of")
    @NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
    public static class EmployeeDeactivated extends DomainEvent<Employee> {

        @AggregateReference
        final Employee employee;

    }
}