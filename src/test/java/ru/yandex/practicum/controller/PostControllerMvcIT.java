package ru.yandex.practicum.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;

import ru.yandex.practicum.WebConfiguration;
import ru.yandex.practicum.configuration.ThymeleafConfiguration;
import ru.yandex.practicum.repository.CommentRepository;
import ru.yandex.practicum.repository.jdbc.JdbcCommentRepository;
import ru.yandex.practicum.repository.jdbc.JdbcPostRepository;
import ru.yandex.practicum.service.impl.BlogServiceImpl;
import ru.yandex.practicum.testconfig.TestDbConfig;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@WebAppConfiguration
@ExtendWith(SpringExtension.class)
@ContextHierarchy({
        @ContextConfiguration(classes = {
                TestDbConfig.class,
                JdbcPostRepository.class,
                JdbcCommentRepository.class,
                BlogServiceImpl.class
        }),
        @ContextConfiguration(classes = {
                WebConfiguration.class,
                ThymeleafConfiguration.class
        })
})
class PostControllerMvcIT {

    @Autowired WebApplicationContext wac;
    @Autowired CommentRepository comments;

    org.springframework.test.web.servlet.MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = webAppContextSetup(wac).build();
    }

    // GET "posts" - список постов на странице ленты постов
    @Test
    void getPosts_defaults_ok_modelAndPaging() throws Exception {
        //создадим один пост
        mvc.perform(multipart("/posts")
                .param("title", "T1")
                .param("tags", "a,b")
                .param("text", "Body"));

        mvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts"))
                .andExpect(model().attributeExists("posts", "paging", "search"))
                .andExpect(model().attribute("search", isEmptyString()))
                .andExpect(model().attribute("paging",
                        allOf(
                                hasProperty("pageNumber", equalTo(1)),
                                hasProperty("pageSize",   equalTo(10))
                        )));
    }

    /*
    GET "posts" - список постов на странице ленты постов (c фильтром и паджинацией)
		Параметры:
			search - строка с поиском по тегу поста (по умолчанию, пустая строка - все посты)
            		pageSize - максимальное число постов на странице (по умолчанию, 10)
            		pageNumber - номер текущей страницы (по умолчанию, 1)
            	Возвращает:
            		шаблон "posts.html"
            		используется модель для заполнения шаблона:
            			"posts" - List<Post> - список постов (id, title, text, imagePath, likesCount, comments)
            			"search" - строка поиска (по умолчанию, пустая строка - все посты)
            			"paging":
            				"pageNumber" - номер текущей страницы (по умолчанию, 1)
            				"pageSize" - максимальное число постов на странице (по умолчанию, 10)
            				"hasNext" - можно ли пролистнуть вперед
            				"hasPrevious" - можно ли пролистнуть назад
     */
    @Test
    void getPosts_withParams_ok() throws Exception {
        mvc.perform(get("/posts")
                        .param("search", "java")
                        .param("pageNumber", "2") // 1-based
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts"))
                .andExpect(model().attributeExists("posts", "paging", "search"))
                .andExpect(model().attribute("search", equalTo("java")))
                .andExpect(model().attribute("paging",
                        allOf(
                                hasProperty("pageNumber", equalTo(2)),
                                hasProperty("pageSize",   equalTo(20))
                        )));
    }

    /*
        GET "/posts/{id}" - страница с постом
        	Возвращает:
       			шаблон "post.html"
       			используется модель для заполнения шаблона:
       				"post" - модель поста (id, title, text, imagePath, likesCount,
     */
    @Test
    void showPost_ok_viewAndModel() throws Exception {
        var create = mvc.perform(multipart("/posts")
                        .param("title", "T")
                        .param("tags", "x")
                        .param("text", "A\nB"))
                .andReturn();
        String location = create.getResponse().getRedirectedUrl(); // "/posts/{id}"

        mvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(view().name("post"))
                .andExpect(model().attributeExists("post"));
    }

    /*
        GET "/posts/add" - страница добавления поста
       		Возвращает:
       			шаблон "add-post.html"
     */
    @Test
    void getAdd_returns_addPost() throws Exception {
        mvc.perform(get("/posts/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-post"));
    }

    /*
        POST "/posts" - добавление поста
       		Принимает:
       			"multipart/form-data"
       		Параметры:
       			"title" - название поста
       			"text" - текст поста
       			"image" - файл картинки поста (класс MultipartFile)
       			"tags" - список тегов поста (по умолчанию, пустая строка)
       		Возвращает:
       			редирект на созданный "/posts/{id}"
     */
    @Test
    void createPost_multipart_redirectsToShow() throws Exception {
        mvc.perform(multipart("/posts")
                        .file("image", new byte[]{1})
                        .param("title", "T")
                        .param("tags", "a,b")
                        .param("text", "X"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/posts/*"));
    }

    /*
        GET "/images/{id}" -эндпоин, возвращающий набор байт картинки поста
       		Параметры:
       			"id" - идентификатор поста
     */
    @Test
    void getImage_returnsBytes() throws Exception {
        var create = mvc.perform(multipart("/posts")
                        .file("image", new byte[]{9, 9, 9})
                        .param("title", "Pic")
                        .param("tags", "img")
                        .param("text", "img"))
                .andReturn();
        String location = create.getResponse().getRedirectedUrl(); // "/posts/{id}"
        String id = location.substring(location.lastIndexOf('/') + 1);

        mvc.perform(get("/images/{id}", id))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", startsWith("image/")))
                .andExpect(content().bytes(new byte[]{9, 9, 9}));
    }

    /*
        POST "/posts/{id}/like" - увеличение/уменьшение числа лайков поста
       		Параметры:
       			"id" - идентификатор поста
       			"like" - если true, то +1 лайк, если "false", то -1 лайк
       		Возвращает:
       			редирект на "/posts/{id}"
     */
    @Test
    void likePost_redirectsBack() throws Exception {
        String loc = mvc.perform(multipart("/posts")
                        .param("title", "L")
                        .param("tags", "x")
                        .param("text", "t"))
                .andReturn().getResponse().getRedirectedUrl();

        mvc.perform(post(loc + "/like").param("like", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(loc));
    }

    /*
        POST "/posts/{id}/edit" - страница редактирования поста
       		Параметры:
       			"id" - идентификатор поста
       		Возвращает:
       			редирект на форму редактирования поста "add-post.html"
       			используется модель для заполнения шаблона:
       				"post" - модель поста (id, title, text, imagePath, likesCount, comments)
     */
    @Test
    void editRedirect_post_to_get_edit() throws Exception {
        String loc = mvc.perform(multipart("/posts")
                        .param("title", "E")
                        .param("tags", "x")
                        .param("text", "t"))
                .andReturn().getResponse().getRedirectedUrl();

        mvc.perform(post(loc + "/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(loc + "/edit"));

        mvc.perform(get(loc + "/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-post"))
                .andExpect(model().attributeExists("post"));
    }

    /*
        POST "/posts/{id}" - редактирование поста (без изменения картинки)
       		Принимает:
       			"multipart/form-data"
       		Параметры:
       			"id" - идентификатор поста
       			"title" - название поста
       			"text" - текст поста
       			"image" - файл картинки поста (класс MultipartFile, может быть null - значит, остается прежним)
       			"tags" - список тегов поста (по умолчанию, пустая строка)
       		Возвращает:
       			редирект на отредактированный "/posts/{id}"
     */
    @Test
    void editPost_withoutNewImage_keepsOldImage() throws Exception {
        String loc = mvc.perform(multipart("/posts")
                        .file("image", new byte[]{5})
                        .param("title", "T")
                        .param("tags", "x")
                        .param("text", "t"))
                .andReturn().getResponse().getRedirectedUrl();
        String id = loc.substring(loc.lastIndexOf('/') + 1);

        mvc.perform(multipart("/posts/{id}", id)
                        .param("title", "T2")
                        .param("tags", "x")
                        .param("text", "t2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(loc));

        mvc.perform(get("/images/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().bytes(new byte[]{5}));
    }

    /*
        POST "/posts/{id}" - редактирование поста (новая картинка)
       		Принимает:
       			"multipart/form-data"
       		Параметры:
       			"id" - идентификатор поста
       			"title" - название поста
       			"text" - текст поста
       			"image" - файл картинки поста (класс MultipartFile, может быть null - значит, остается прежним)
       			"tags" - список тегов поста (по умолчанию, пустая строка)
       		Возвращает:
       			редирект на отредактированный "/posts/{id}"
     */
    @Test
    void editPost_withNewImage_replacesImage() throws Exception {
        String loc = mvc.perform(multipart("/posts")
                        .file("image", new byte[]{1})
                        .param("title", "T")
                        .param("tags", "x")
                        .param("text", "t"))
                .andReturn().getResponse().getRedirectedUrl();
        String id = loc.substring(loc.lastIndexOf('/') + 1);

        mvc.perform(multipart("/posts/{id}", id)
                        .file("image", new byte[]{2})
                        .param("title", "T2")
                        .param("tags", "x")
                        .param("text", "t2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(loc));

        mvc.perform(get("/images/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().bytes(new byte[]{2}));
    }

    /*
        POST "/posts/{id}/comments" - эндпоинт добавления комментария к посту
       		Параметры:
       			"id" - идентификатор поста
       			"text" - текст комментария
       		Возвращает:
       			редирект на "/posts/{id}"
     */
    @Test
    void addComment_redirectsBack() throws Exception {
        String loc = mvc.perform(multipart("/posts")
                        .param("title", "C")
                        .param("tags", "x")
                        .param("text", "t"))
                .andReturn().getResponse().getRedirectedUrl();

        mvc.perform(post(loc + "/comments").param("text", "hello"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(loc));
    }

    /*
        -POST "/posts/{id}/comments/{commentId}" - эндпоинт редактирования комментария
       		Параметры:
       			"id" - идентификатор поста
       			"commentId" - идентификатор комментария
       			"text" - текст комментария
       		Возвращает:
       			редирект на "/posts/{id}"
       - POST "/posts/{id}/comments/{commentId}/delete" - эндпоинт удаления комментария
       		Параметры:
       			"id" - идентификатор поста
       			"commentId" - идентификатор комментария
       		Возвращает:
       			редирект на "/posts/{id}"
     */
    @Test
    void editAndDeleteComment_redirectsBack() throws Exception {
        String loc = mvc.perform(multipart("/posts")
                        .param("title", "C2")
                        .param("tags", "x")
                        .param("text", "t"))
                .andReturn().getResponse().getRedirectedUrl();
        String postId = loc.substring(loc.lastIndexOf('/') + 1);

        // add
        mvc.perform(post("/posts/{id}/comments", postId).param("text", "c1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + postId));

        long pid = Long.parseLong(postId);
        var list = comments.findByPostId(pid);
        long cid = list.isEmpty() ? 1L : list.get(0).getId();

        mvc.perform(post("/posts/{id}/comments/{cid}", postId, cid).param("text", "upd"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + postId));

        mvc.perform(post("/posts/{id}/comments/{cid}/delete", postId, cid))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + postId));
    }

    /*
    POST "/posts/{id}/delete" - эндпоинт удаления поста
       		Параметры:
       			"id" - идентификатор поста
       		Возвращает:
       			редирект на "/posts"
     */
    @Test
    void deletePost_redirectsToFeed() throws Exception {
        String loc = mvc.perform(multipart("/posts")
                        .param("title", "D")
                        .param("tags", "x")
                        .param("text", "t"))
                .andReturn().getResponse().getRedirectedUrl();
        String id = loc.substring(loc.lastIndexOf('/') + 1);

        mvc.perform(post("/posts/{id}/delete", id))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }
}
