package net.slipp.presentation;

import static com.google.common.base.Charsets.*;
import static kr.wootecat.dongle.model.http.MimeType.*;

import java.io.IOException;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.slipp.application.UserService;
import net.slipp.application.UserServiceFactory;
import net.slipp.presentation.dto.UsersResponse;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import kr.wootecat.dongle.model.http.request.HttpRequest;
import kr.wootecat.dongle.model.http.response.HttpResponse;
import kr.wootecat.dongle.model.servlet.HttpServlet;
import utils.FileIoUtils;

public class UserListServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(LoginServlet.class);

    private static final String REQUIRE_AUTH_PAGE_URL = "./templates/user/login.html";
    private static final String LOGINED = "logined";

    @Override
    protected void doGet(HttpRequest request, HttpResponse response) {
        boolean logined = request.hasCookie(LOGINED, true);
        if (!logined) {
            try {
                response.addBody(FileIoUtils.loadFileFromClasspath(REQUIRE_AUTH_PAGE_URL), HTML_UTF_8);
            } catch (IOException | URISyntaxException e) {
                logger.error(e.getMessage());
            }
            return;
        }

        UserService userService = UserServiceFactory.getInstance();
        UsersResponse userResponse = userService.findAll();

        TemplateLoader loader = new ClassPathTemplateLoader("/templates", ".html");
        Handlebars handlebars = new Handlebars(loader);

        try {
            Template template = handlebars.compile("user/list");
            String userListPage = template.apply(userResponse);
            response.addBody(userListPage.getBytes(UTF_8), HTML_UTF_8);
            logger.debug("{}", userListPage);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
