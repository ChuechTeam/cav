package cy.cav.framework;

import com.netflix.appinfo.*;
import org.springframework.beans.*;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.beans.factory.config.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.web.context.*;
import org.springframework.cloud.commons.util.*;
import org.springframework.cloud.netflix.eureka.*;
import org.springframework.context.*;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.*;
import org.springframework.lang.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.reactive.config.*;

import java.util.*;

/// Configuration of the library for Spring Boot. Also configures Eureka.
@AutoConfiguration
@ComponentScan
@EnableScheduling
@PropertySource("classpath:application.properties")
public class FrameworkConfig {
    @Bean
    BeanPostProcessor eurekaInstanceConfigCustomizer(Server server) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof EurekaInstanceConfigBean instanceConfigBean) {
                    instanceConfigBean.setInstanceId(server.idString());
                }
                return bean;
            }
        };
    }
}