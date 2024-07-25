package com.ardkyer.rion.service;

import com.ardkyer.rion.entity.User;
import com.ardkyer.rion.repository.UserRepository;
import com.ardkyer.rion.security.PrincipalDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerId;

        if ("google".equals(provider)) {
            providerId = oauth2User.getAttribute("sub");
        } else if ("kakao".equals(provider)) {
            Map<String, Object> attributes = oauth2User.getAttributes();
            providerId = String.valueOf(attributes.get("id")); // Explicitly convert to String
        } else {
            throw new OAuth2AuthenticationException("Unsupported provider: " + provider);
        }

        String username = provider + "_" + providerId;

        User user = userRepository.findByUsername(username)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername(username);
                    newUser.setProvider(provider);
                    newUser.setProviderId(providerId);
                    return userRepository.save(newUser);
                });

        return new PrincipalDetails(user, oauth2User.getAttributes());
    }
}