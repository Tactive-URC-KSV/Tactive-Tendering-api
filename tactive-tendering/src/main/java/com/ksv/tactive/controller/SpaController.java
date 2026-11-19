package com.ksv.tactive.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

	@GetMapping({
			"/tendering/contractor",
			"/tendering/contractor/",
			"/tendering/contractor/{path:^(?!assets$)[^.]+}",
			"/tendering/contractor/{path:^(?!assets$)[^.]+}/**"
	})
	public String contractor() {
		return "forward:/tendering/contractor/index.html";
	}

	@GetMapping({
			"/tendering",
			"/tendering/",
			"/tendering/{path:^(?!tactive$)(?!contractor$)(?!assets$)[^.]+}",
			"/tendering/{path:^(?!tactive$)(?!contractor$)(?!assets$)[^.]+}/**"
	})
	public String tender() {
		return "forward:/tendering/index.html";
	}
}
