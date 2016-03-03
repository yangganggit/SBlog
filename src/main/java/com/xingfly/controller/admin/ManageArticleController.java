package com.xingfly.controller.admin;

import com.xingfly.model.Article;
import com.xingfly.util.Pager;
import com.xingfly.model.dto.ArticleDto;
import com.xingfly.model.dto.UserDto;
import com.xingfly.service.ArticleService;
import com.xingfly.service.CategoryService;
import com.xingfly.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by SuperS on 16/2/29.
 * 管理员 文章编辑页面
 */
@Controller
@RequestMapping("manage/article")
public class ManageArticleController {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ArticleService articleService;

    //显示创建页面
    @RequestMapping(value = "create", method = RequestMethod.GET)
    public String showCreatePage(ModelMap model, HttpSession session) {
        model.addAttribute("mainPage", "admin/article/editorArticle.vm");
        model.addAttribute("user", (UserDto) session.getAttribute("currentUser"));
        model.addAttribute("categories", categoryService.getCategories());
        return "admin/index";
    }

    //创建操作
    @RequestMapping(value = "create", method = RequestMethod.POST)
    public String createAction(Article article, ModelMap model, HttpSession session) {
        String path;
        article.setClicks(0);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        article.setPubDate(formatter.format(new Date()));
        if (StringUtil.isNotEmpty(article.getTitle()) && StringUtil.isNotEmpty(article.getMarkDown()) && StringUtil.isNotEmpty(article.getRemark())) {
            articleService.saveArticle(article);
            path = "redirect:/manage/article";
        } else {
            model.addAttribute("error", "有未填选项,请核对后重新发布文章!");
            model.addAttribute("mainPage", "admin/article/editorArticle.vm");
            model.addAttribute("user", (UserDto) session.getAttribute("currentUser"));
            model.addAttribute("categories", categoryService.getCategories());
            path = "admin/index";
        }
        return path;
    }

    //显示文章列表
    @RequestMapping(method = RequestMethod.GET)
    public String showListArticle(ModelMap model, @RequestParam(defaultValue = "1") Integer currentPage, HttpSession session) {
        Pager pager = new Pager(currentPage, 10, articleService.count());
        List<ArticleDto> articles = articleService.getPageArticles(pager);
        model.addAttribute("articles", articles);
        model.addAttribute("pager", pager);
        model.addAttribute("user", (UserDto) session.getAttribute("currentUser"));
        model.addAttribute("mainPage", "admin/article/listArticle.vm");
        return "admin/index";
    }

    //通过 ID 显示更新文章页面
    @RequestMapping(value = "update/{articleId:[0-9]+}", method = RequestMethod.GET)
    public String upDateArticle(ModelMap model, HttpSession session, @PathVariable("articleId") Integer articleId) {
        model.addAttribute("user", (UserDto) session.getAttribute("currentUser"));
        ArticleDto articleDto = articleService.getArticle(articleId);
        if (StringUtil.isNotEmpty(articleDto.getTitle())) {
            model.addAttribute("article", articleDto);
            model.addAttribute("mainPage", "admin/article/editorArticle.vm");
            model.addAttribute("categories", categoryService.getCategories());
        } else {
            Pager pager = new Pager(1, 10, articleService.count());
            List<ArticleDto> articles = articleService.getPageArticles(pager);
            model.addAttribute("error", "找不到该文章!");
            model.addAttribute("mainPage", "admin/article/listArticle.vm");
            model.addAttribute("pager", pager);
            model.addAttribute("articles", articles);
        }
        return "admin/index";
    }

    //通过ID更新文章 操作
    @RequestMapping(value = "update/{articleId:[0-9]+}", method = RequestMethod.POST)
    public String upDateArticleAction(Article article, @PathVariable("articleId") Integer articleId, ModelMap model, HttpSession session) {
        String path;
        if (StringUtil.isNotEmpty(article.getTitle()) && StringUtil.isNotEmpty(article.getMarkDown()) && StringUtil.isNotEmpty(article.getRemark())) {
            article.setClicks(articleService.getArticle(articleId).getClicks());
            article.setPubDate(articleService.getArticle(articleId).getPubDate());
            articleService.updateArticle(article);
            path = "redirect:/manage/article";
        } else {
            model.addAttribute("error", "有未填选项,请核对后重新发布文章!");
            model.addAttribute("mainPage", "admin/article/editorArticle.vm");
            model.addAttribute("user", (UserDto) session.getAttribute("currentUser"));
            model.addAttribute("article", articleService.getArticle(articleId));
            model.addAttribute("categories", categoryService.getCategories());
            path = "admin/index";
        }
        return path;
    }

    //通过ID 删除文章
    @RequestMapping("delete/{articleId:[0-9]+}")
    public String deleteArticle(@PathVariable("articleId") Integer articleId, @RequestParam(value = "currentPage", defaultValue = "1") Integer currentPage) {
        String path = "redirect:/manage/article";
        articleService.deleteArticle(articleId);
        if (currentPage != 1) {
            path = "redirect:/manage/article/?currentPage=" + currentPage;
        }
        return path;
    }

    //搜索 文章
    @RequestMapping("search")
    public String search(String content, ModelMap model, HttpSession session) {
        Article article = new Article();
        article.setTitle(content);
        Pager pager = new Pager(1, articleService.searchArticles(article).size(), articleService.searchArticles(article).size());
        List<ArticleDto> articleDtos = articleService.searchArticles(article);
        model.addAttribute("user", (UserDto) session.getAttribute("currentUser"));
        model.addAttribute("pager", pager);
        model.addAttribute("articles", articleDtos);
        model.addAttribute("mainPage", "admin/article/listArticle.vm");
        return "admin/index";
    }
}