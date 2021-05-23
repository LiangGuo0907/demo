package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author wl
 * @date 2021/5/23 11:03
 */
@Controller
public class DemoController {

    @RequestMapping("index")
    public String index(Model model){
        model.addAttribute("name","jack");
        model.addAttribute("age","30");
        model.addAttribute("info","我是一个爱学习的好孩子");
        return "index";
    }

    @RequestMapping(value = "/upload",method = RequestMethod.POST)
    @ResponseBody
    public String upload(@RequestParam(value="file",required = false) MultipartFile file, HttpServletRequest request, HttpServletResponse response){
        String a = "";
        return "index";
    }
}
