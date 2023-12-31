package br.com.lhpsdev.todolist.filter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.lhpsdev.todolist.user.IUserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

        var servletPath = request.getServletPath();

        if (servletPath.startsWith("/tasks")) {
            var authorization = request.getHeader("Authorization");

            if (authorization.length() == 0) {
                response.sendError(401, "Usuário sem autorização");
            }
            var authEncoded = authorization.substring("Basic".length()).trim();
            byte[] authDecoded = Base64.getDecoder().decode(authEncoded);
            var authString = new String(authDecoded);

            String[] credentials = authString.split(":");
            String username = credentials[0];
            String password = credentials[1];

            var user = this.userRepository.findByUsername(username);

            if (user == null) {
                response.sendError(401, "Usuário sem autorização");
            } else {
                var passwordVerified = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
                if (passwordVerified.verified) {
                    request.setAttribute("idUser", user.getId());
                    chain.doFilter(request, response);
                } else {
                    response.sendError(401, "Usuário sem autorização");
                }
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}
