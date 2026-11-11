package paypulse.api.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI payPulseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PayPulse API")
                        .version("v1")
                        .description("API de consulta e processamento de transações de pagamento."));
    }
}
