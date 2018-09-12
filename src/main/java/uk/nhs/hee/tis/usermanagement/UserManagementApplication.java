package uk.nhs.hee.tis.usermanagement;

import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import uk.nhs.hee.tis.usermanagement.controllers.FormValues;
import uk.nhs.hee.tis.usermanagement.model.HeeUser;

import java.util.List;

@SpringBootApplication
@PropertySource(
    {
        "classpath:/application.properties",
        "classpath:/config/profileclientapplication.properties"
    }
)
public class UserManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserManagementApplication.class, args);
	}
}
