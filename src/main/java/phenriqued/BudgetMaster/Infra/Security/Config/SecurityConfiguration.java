package phenriqued.BudgetMaster.Infra.Security.Config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import phenriqued.BudgetMaster.Infra.Security.Filters.TokenAccessFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final TokenAccessFilter tokenAccessFilter;

    public SecurityConfiguration(TokenAccessFilter tokenAccessFilter) {
        this.tokenAccessFilter = tokenAccessFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(tokenAccessFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(
                        authRequest -> {
                            authRequest.requestMatchers("/h2-console/**").permitAll();
                            authRequest.requestMatchers("/login/**", "account/manager/change-password-to-activate").permitAll();
                            authRequest.requestMatchers("/account/two-factor-authentication/resend").permitAll();
                            authRequest.anyRequest().authenticated();
                        }
                )
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}
