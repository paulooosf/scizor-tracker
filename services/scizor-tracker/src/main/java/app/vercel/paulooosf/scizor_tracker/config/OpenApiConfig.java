package app.vercel.paulooosf.scizor_tracker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI scizorTrackerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Scizor Tracker API")
                        .description("""
                                API REST para gerenciamento de bugs e rastreamento de problemas em projetos de software.
                                
                                ## Recursos Principais:
                                - **Autenticação JWT** com roles (ADMIN/USER)
                                - **Gerenciamento de Bugs** com prioridades e status
                                - **Projetos** organizados por contexto
                                - **Comentários** para colaboração em bugs
                                - **Usuários** com controle de acesso
                                
                                ## Autenticação:
                                1. Faça login em `/api/autenticar/login` com email e senha
                                2. Copie o token retornado
                                3. Clique no botão **Authorize** (topo da página)
                                4. Cole o token (apenas o valor, sem "Bearer")
                                5. Clique em **Authorize** e depois **Close**
                                
                                ## Permissões:
                                - **USER**: Apenas leitura (GET)
                                - **ADMIN**: Leitura e escrita completa
                                
                                ## Credenciais de Teste:
                                - **Admin**: `admin@scizor.com` / `admin123`
                                - **User**: `joao.silva@example.com` / `senha123`
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Paulo Henrique")
                                .email("paulooosf@gmail.com")
                                .url("https://paulooosf.vercel.app"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor Local de Desenvolvimento")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .name("Bearer Authentication")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Insira o token JWT obtido no endpoint de login")));
    }
}
