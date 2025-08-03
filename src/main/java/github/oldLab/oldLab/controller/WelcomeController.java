package github.oldLab.oldLab.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WelcomeController {

    @GetMapping("/")
    public String welcome() {
        return "welcome to the Old Lab.kz. this is a testing server. you can get more information about the project at: https://t.me/tggurov.\n" +
               "server owner is tg: @axgiri. if you have any questions or problems, feel free to ask.\n" +
               "server is only for developers and testers, not for public use.\n";
    }
}
//TODO: do not add it to the master branch, this is just for testing purposes