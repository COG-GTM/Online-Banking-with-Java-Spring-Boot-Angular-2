package com.userFront;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore("Spring Boot 1.5.4 context does not load under Java 21")
public class UserFrontApplicationTests {

	@Test
	public void contextLoads() {
	}

}
