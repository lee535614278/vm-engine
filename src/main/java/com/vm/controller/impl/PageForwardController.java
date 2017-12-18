package com.vm.controller.impl;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * 页面转发
 */
@Controller
@RequestMapping("/page")
public class PageForwardController {

    @RequestMapping("/f/{name}")
    public Object frontend(@PathVariable("name") String name) {
        return "frontend/"+name;
    }
    @RequestMapping("/b/{name}")
    public Object backend(@PathVariable("name") String name) {
        return "backend/"+name;
    }


}