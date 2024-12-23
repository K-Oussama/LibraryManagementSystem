package com.sos.lms.selenium;

import org.junit.jupiter.api.AfterEach;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public abstract class BaseSeleniumTest {

    @Autowired
    protected WebDriver driver;

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}