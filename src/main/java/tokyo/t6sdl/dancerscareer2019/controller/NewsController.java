package tokyo.t6sdl.dancerscareer2019.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.vladsch.flexmark.ext.attributes.AttributesExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.ins.InsExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

import lombok.RequiredArgsConstructor;
import tokyo.t6sdl.dancerscareer2019.httpresponse.NotFound404;
import tokyo.t6sdl.dancerscareer2019.model.Account;
import tokyo.t6sdl.dancerscareer2019.service.AccountService;
import tokyo.t6sdl.dancerscareer2019.service.SecurityService;

@RequiredArgsConstructor
@Controller
@RequestMapping("/news")
public class NewsController {
	private final SecurityService securityService;
	private final AccountService accountService;
	private final List<Map<String, Object>> newsList = generateNewsList();

	@RequestMapping()
	public String index(Model model) {
		Account account = accountService.getAccountByEmail(securityService.findLoggedInEmail());
		if (Objects.equals(account, null)) {
			model.addAttribute("header", "for-stranger");
		} else if (account.isAdmin()) {
			model.addAttribute("header", "for-admin");
		} else {
			model.addAttribute("header", "for-user");
		}
		model.addAttribute("newsList", newsList);
		return "news/index";
	}

	@RequestMapping("/{newsId}")
	public String show(@PathVariable("newsId") Integer newsId, Model model) {
		Account account = accountService.getAccountByEmail(securityService.findLoggedInEmail());
		if (Objects.equals(account, null)) {
			model.addAttribute("header", "for-stranger");
		} else if (account.isAdmin()) {
			model.addAttribute("header", "for-admin");
		} else {
			model.addAttribute("header", "for-user");
		}
		MutableDataSet options = new MutableDataSet();
		options.set(Parser.EXTENSIONS,
				Arrays.asList(
						AttributesExtension.create(),
						AutolinkExtension.create(),
						EmojiExtension.create(),
						InsExtension.create(),
						StrikethroughExtension.create(),
						TablesExtension.create()
				));
		options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");
		Parser parser = Parser.builder(options).build();
		HtmlRenderer renderer = HtmlRenderer.builder(options).build();
		Path path = Paths.get("src/main/resources/static/md/" + newsId + ".md");
		List<String> lines;
		try {
			lines = Files.readAllLines(path, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
			throw new NotFound404();
		}
		Node document = parser.parse(lines.stream().collect(Collectors.joining("\n")));
		String html = renderer.render(document);
		model.addAttribute("md", html);
		model.addAttribute("title", "ダンサー向け就活セミナーが開催されます！");
		model.addAttribute("updated_at", LocalDate.of(2019, 2, 13));
		return "news/show";
	}
	
	private List<Map<String, Object>> generateNewsList() {
		List<Map<String, Object>> newsList = new ArrayList<Map<String, Object>>();
		List<Integer> ids = Arrays.asList(1, 11);
		List<String> titles = Arrays.asList("ダンサー向け就活セミナーが開催されます！", "第1回「人事がナットクするダンスの伝え方セミナー」レポート");
		List<LocalDate> datetimes = Arrays.asList(LocalDate.of(2018, 12, 6), LocalDate.of(2019, 2, 6));
		for (int i = 0; i < ids.size(); i++) {
			Map<String, Object> news = new HashMap<String, Object>();
			news.put("id", ids.get(i));
			news.put("title", titles.get(i));
			news.put("updated_at", datetimes.get(i));
			newsList.add(news);
		}
		return newsList.stream().sorted((n1, n2) -> {
			LocalDate updated_at1 = (LocalDate) n1.get("updated_at");
			LocalDate updated_at2 = (LocalDate) n2.get("updated_at");
			int ret = updated_at2.compareTo(updated_at1);
			return ret == 0 ? -1 : ret;
		}).collect(Collectors.toList());
	}
}
