package ru.yandex.practicum.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import ru.yandex.practicum.WebConfiguration;
import ru.yandex.practicum.configuration.ThymeleafConfiguration;
import ru.yandex.practicum.testconfig.TestDbConfig;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextHierarchy({
        @ContextConfiguration(classes = { TestDbConfig.class }),
        @ContextConfiguration(classes = { WebConfiguration.class, ThymeleafConfiguration.class, RootController.class })
})
class RootControllerMvcIT {
    @org.springframework.beans.factory.annotation.Autowired org.springframework.web.context.WebApplicationContext wac;

    //GET "/" - редирект на "/posts"
    @Test
    void rootRedirectsToPostsFeed() throws Exception {
        var mvc = webAppContextSetup(wac).build();
        mvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }
}
