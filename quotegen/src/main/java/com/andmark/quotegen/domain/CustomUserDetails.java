//package com.andmark.quotegen.domain;
//
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//
//import java.util.Collection;
//import java.util.Collections;
//import java.util.stream.Collectors;
//
//public class CustomUserDetails extends User implements UserDetails {
//
//    private final User user;
//
//    public CustomUserDetails(User user) {
//        this.user = user;
//    }
//
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()));
//}
//
//@Override
//public String getPassword() {
//    return super.getPassword();
//}
//
//@Override
//public String getUsername() {
//    return super.getUsername();
//}
//
//@Override
//public boolean isAccountNonExpired() {
//    return true;
//}
//
//@Override
//public boolean isAccountNonLocked() {
//    return true;
//}
//
//@Override
//public boolean isCredentialsNonExpired() {
//    return true;
//}
//
//@Override
//public boolean isEnabled() {
//    return true;
//}
//}
