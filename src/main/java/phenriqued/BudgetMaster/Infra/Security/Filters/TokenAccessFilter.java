package phenriqued.BudgetMaster.Infra.Security.Filters;

import com.sun.net.httpserver.Headers;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import phenriqued.BudgetMaster.Infra.Exceptions.Exception.BudgetMasterSecurityException;
import phenriqued.BudgetMaster.Infra.Security.Service.TokenService;
import phenriqued.BudgetMaster.Infra.Security.User.UserDetailsImpl;
import phenriqued.BudgetMaster.Repositories.UserRepository.UserRepository;

import java.io.IOException;
import java.util.Objects;

@Component
public class TokenAccessFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserRepository userRepository;

    public TokenAccessFilter(TokenService tokenService, UserRepository userRepository) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = recoverRequestToken(request);

        if (Objects.nonNull(token)) {
            String subject = tokenService.validationTokenJWT(token);
            var user = new UserDetailsImpl(userRepository.findByEmail(subject)
                            .orElseThrow(() -> new BudgetMasterSecurityException("[ERROR] Invalid Token. User cannot be found!")));

            Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private String recoverRequestToken(HttpServletRequest request){
        var authorization = request.getHeader("Authorization");

        if(Objects.nonNull(authorization)) return authorization.replace("Bearer ","");

        return null;
    }

}
