package tokyo.t6sdl.dancerscareer2019.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import lombok.RequiredArgsConstructor;
import tokyo.t6sdl.dancerscareer2019.httpresponse.NotFound404;
import tokyo.t6sdl.dancerscareer2019.io.ExcelBuilder;
import tokyo.t6sdl.dancerscareer2019.model.Es;
import tokyo.t6sdl.dancerscareer2019.model.Experience;
import tokyo.t6sdl.dancerscareer2019.model.Interview;
import tokyo.t6sdl.dancerscareer2019.model.Profile;
import tokyo.t6sdl.dancerscareer2019.model.Student;
import tokyo.t6sdl.dancerscareer2019.model.form.EsForm;
import tokyo.t6sdl.dancerscareer2019.model.form.ExperienceForm;
import tokyo.t6sdl.dancerscareer2019.model.form.InterviewForm;
import tokyo.t6sdl.dancerscareer2019.service.ExperienceService;
import tokyo.t6sdl.dancerscareer2019.service.ProfileService;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin")
public class AdminController {
	private final ProfileService profileService;
	private final ExperienceService experienceService;
	
	@GetMapping()
	public String index() {
		return "admin/index";
	}
	
	@GetMapping(value = "/users")
	public String usersIndex(@RequestParam(name = "sort", defaultValue = "0") Integer sort, Model model) {
		Map<String, Object> result = profileService.getProfiles(sort);
		model.addAttribute("count", result.get("count"));
		model.addAttribute("students", result.get("students"));
		model.addAttribute("sort", sort);
		model.addAttribute("positionList", Profile.POSITION_LIST);
		return "admin/users/index";
	}
	
	@GetMapping(value = "/users", params = "download")
	public ModelAndView downloadUsersIndex(@RequestParam(name = "sort", defaultValue = "0") Integer sort) {
		Map<String, Object> map = new HashMap<String, Object>();
		List<String> filter = Arrays.asList("なし", "-");
		@SuppressWarnings("unchecked")
		List<Student> students = (List<Student>) profileService.getProfiles(sort).get("students");
		map.put("filter", filter);
		map.put("students", students);
		LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Tokyo"));
		String today = now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
		ModelAndView mav = new ModelAndView(new ExcelBuilder(today + "_students" + ".xlsx"), map);
		return mav;
	}
	
	@GetMapping(value = "/users", params = {"kana-family-name", "kana-given-name"})
	public String usersIndexFilteredByName(@RequestParam(name = "sort", defaultValue = "0") Integer sort, @RequestParam("kana-family-name") String kanaFamilyName, @RequestParam("kana-given-name") String kanaGivenName, Model model) {
		Map<String, Object> result = new HashMap<String, Object>();
		if (!(kanaFamilyName.isEmpty()) && !(kanaGivenName.isEmpty())) {
			result = profileService.getProfilesByName(sort, kanaFamilyName, kanaGivenName);
		} else if (!(kanaFamilyName.isEmpty())) {
			result = profileService.getProfilesByLastName(sort, kanaFamilyName);
		} else {
			return "redirect:/admin/users?sort=" + sort;
		}
		model.addAttribute("count", result.get("count"));
		model.addAttribute("students", result.get("students"));
		model.addAttribute("sort", sort);
		model.addAttribute("kanaFamilyName", kanaFamilyName);
		model.addAttribute("kanaGivenName", kanaGivenName);
		model.addAttribute("positionList", Profile.POSITION_LIST);
		return "admin/users/index";
	}
	
	@SuppressWarnings("unchecked")
	@GetMapping(value = "/users", params = {"kana-family-name", "kana-given-name", "download"})
	public ModelAndView downloadUsersIndexFilteredByName(@RequestParam(name = "sort", defaultValue = "0") Integer sort, @RequestParam("kana-family-name") String kanaFamilyName, @RequestParam("kana-given-name") String kanaGivenName) {
		Map<String, Object> map = new HashMap<String, Object>();
		List<String> filter = Arrays.asList("氏名(カナ)", kanaFamilyName + "," + kanaGivenName);
		List<Student> students = new ArrayList<Student>();
		if (!(kanaFamilyName.isEmpty()) && !(kanaGivenName.isEmpty())) {
			students = (List<Student>) profileService.getProfilesByName(sort, kanaFamilyName, kanaGivenName).get("students");
		} else if (!(kanaFamilyName.isEmpty())) {
			students = (List<Student>) profileService.getProfilesByLastName(sort, kanaFamilyName).get("students");
		}
		map.put("filter", filter);
		map.put("students", students);
		LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Tokyo"));
		String today = now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
		ModelAndView mav = new ModelAndView(new ExcelBuilder(today + "_students_name" + ".xlsx"), map);
		return mav;
	}
	
	@GetMapping(value = "/users", params = {"univ-loc", "univ-type", "univ-name", "univ-fac", "univ-dep"})
	public String usersIndexFilteredByUniv(@RequestParam(name = "sort", defaultValue = "0") Integer sort, @RequestParam("univ-loc") String univLoc, @RequestParam("univ-name") String univName, @RequestParam("univ-fac") String univFac, @RequestParam("univ-dep") String univDep, Model model) {
		Map<String, Object> result = new HashMap<String, Object>();
		if (!(univDep.isEmpty())) {
			result = profileService.getProfilesByDepartment(sort, univLoc, univName, univFac, univDep);
		} else if (!(univFac.isEmpty())) {
			result = profileService.getProfilesByFaculty(sort, univLoc, univName, univFac);
		} else if (!(univName.isEmpty())) {
			result = profileService.getProfilesByUniversity(sort, univLoc, univName);
		} else if (!(univLoc.isEmpty())) {
			result = profileService.getProfilesByPrefecture(sort, univLoc);
		} else {
			return "redirect:/admin/users?sort=" + sort;
		}
		model.addAttribute("count", result.get("count"));
		model.addAttribute("students", result.get("students"));
		model.addAttribute("sort", sort);
		model.addAttribute("positionList", Profile.POSITION_LIST);
		model.addAttribute("hiddenUnivLoc", univLoc);
		model.addAttribute("hiddenUnivName", univName);
		model.addAttribute("hiddenUnivFac", univFac);
		model.addAttribute("hiddenUnivDep", univDep);
		return "admin/users/index";
	}
	
	@SuppressWarnings("unchecked")
	@GetMapping(value = "/users", params = {"univ-loc", "univ-type", "univ-name", "univ-fac", "univ-dep", "download"})
	public ModelAndView downloadUsersIndexFilteredByUniv(@RequestParam(name = "sort", defaultValue = "0") Integer sort, @RequestParam("univ-loc") String univLoc, @RequestParam("univ-name") String univName, @RequestParam("univ-fac") String univFac, @RequestParam("univ-dep") String univDep) {
		Map<String, Object> map = new HashMap<String, Object>();
		List<String> filter = Arrays.asList("大学", univLoc + "," + univName + "," + univFac + "," + univDep);
		List<Student> students = new ArrayList<Student>();
		if (!(univDep.isEmpty())) {
			students = (List<Student>) profileService.getProfilesByDepartment(sort, univLoc, univName, univFac, univDep).get("students");
		} else if (!(univFac.isEmpty())) {
			students = (List<Student>) profileService.getProfilesByFaculty(sort, univLoc, univName, univFac).get("students");
		} else if (!(univName.isEmpty())) {
			students = (List<Student>) profileService.getProfilesByUniversity(sort, univLoc, univName).get("students");
		} else if (!(univLoc.isEmpty())) {
			students = (List<Student>) profileService.getProfilesByPrefecture(sort, univLoc).get("students");
		}
		map.put("filter", filter);
		map.put("students", students);
		LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Tokyo"));
		String today = now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
		ModelAndView mav = new ModelAndView(new ExcelBuilder(today + "_students_univ" + ".xlsx"), map);
		return mav;
	}
	
	@GetMapping(value = "/users", params = {"pos", "cond=and"})
	public String usersIndexFilteredByPosAndPos(@RequestParam(name = "sort", defaultValue = "0") Integer sort, @RequestParam("pos") List<String> positions, Model model) {
		Map<String, Object> result = new HashMap<String, Object>();
		if (!(positions.isEmpty()) && !(positions.get(0).isEmpty())) {
			result = profileService.getProfilesByPosition(sort, positions, true);
		} else {
			return "redirect:/admin/users?sort=" + sort;
		}
		model.addAttribute("count", result.get("count"));
		model.addAttribute("students", result.get("students"));
		model.addAttribute("sort", sort);
		model.addAttribute("positions", positions);
		model.addAttribute("positionList", Profile.POSITION_LIST);
		return "admin/users/index";
	}
	
	@SuppressWarnings("unchecked")
	@GetMapping(value = "/users", params = {"pos", "cond=and", "download"})
	public ModelAndView downloadUsersIndexFilteredByPosAndPos(@RequestParam(name="sort") Integer sort, @RequestParam(name="pos") List<String> positions) {
		Map<String, Object> map = new HashMap<String, Object>();
		List<String> filter = Arrays.asList("役職(AND)", String.join(",", positions));
		List<Student> students = new ArrayList<Student>();
		if (!(positions.isEmpty()) && !(positions.get(0).isEmpty())) {
			students = (List<Student>) profileService.getProfilesByPosition(sort, positions, true).get("students");
		}
		map.put("filter", filter);
		map.put("students", students);
		LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Tokyo"));
		String today = now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
		ModelAndView mav = new ModelAndView(new ExcelBuilder(today + "_students_pos_and" + ".xlsx"), map);
		return mav;
	}
	
	@GetMapping(value = "/users", params = {"pos", "cond=or"})
	public String usersIndexFilteredByPosOrPos(@RequestParam(name = "sort", defaultValue = "0") Integer sort, @RequestParam("pos") List<String> positions, Model model) {
		Map<String, Object> result = new HashMap<String, Object>();
		if (!(positions.isEmpty()) && !(positions.get(0).isEmpty())) {
			result = profileService.getProfilesByPosition(sort, positions, false);
		} else {
			return "redirect:/admin/users?sort=" + sort;
		}
		model.addAttribute("count", result.get("count"));
		model.addAttribute("students", result.get("students"));
		model.addAttribute("sort", sort);
		model.addAttribute("positions", positions);
		model.addAttribute("positionList", Profile.POSITION_LIST);
		return "admin/users/index";
	}
	
	@SuppressWarnings("unchecked")
	@GetMapping(value = "/users", params = {"pos", "cond=or", "download"})
	public ModelAndView downloadUsersIndexFilteredByPosOrPos(@RequestParam(name="sort") Integer sort, @RequestParam(name="pos") List<String> positions) {
		Map<String, Object> map = new HashMap<String, Object>();
		List<String> filter = Arrays.asList("役職(OR)", String.join(",", positions));
		List<Student> students = new ArrayList<Student>();
		if (!(positions.isEmpty()) && !(positions.get(0).isEmpty())) {
			students = (List<Student>) profileService.getProfilesByPosition(sort, positions, true).get("students");
		}
		students.sort(Comparator.comparing(Student::getLast_login, Comparator.reverseOrder()));
		map.put("filter", filter);
		map.put("students", students);
		LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Tokyo"));
		String today = now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
		ModelAndView mav = new ModelAndView(new ExcelBuilder(today + "_students_pos_or" + ".xlsx"), map);
		return mav;
	}
	
	@GetMapping(value = "/experiences")
	public String expsIndex(@RequestParam(name = "sort", defaultValue = "0") Integer sort, Model model) {
		Map<String, Object> result = experienceService.getExperiences(sort);
		model.addAttribute("count", result.get("count"));
		model.addAttribute("experiences", result.get("experiences"));
		model.addAttribute("sort", sort);
		model.addAttribute("positionList", Profile.POSITION_LIST);
		return "admin/experiences/index";
	}
	
	@GetMapping(value = "/experiences", params = {"kana-family-name", "kana-given-name"})
	public String expsIndexFilteredByName(@RequestParam(name = "sort", defaultValue = "0") Integer sort, @RequestParam("kana-family-name") String kanaFamilyName, @RequestParam("kana-given-name") String kanaGivenName, Model model) {
		Map<String, Object> result = new HashMap<String, Object>();
		if (!(kanaFamilyName.isEmpty()) && !(kanaGivenName.isEmpty())) {
			result = experienceService.getExperiencesByName(sort, kanaFamilyName, kanaGivenName);
		} else if (!(kanaFamilyName.isEmpty())) {
			result = experienceService.getExperiencesByLastName(sort, kanaFamilyName);
		} else {
			return "redirect:/admin/experiences?sort=" + sort;
		}
		model.addAttribute("count", result.get("count"));
		model.addAttribute("experiences", result.get("experiences"));
		model.addAttribute("sort", sort);
		model.addAttribute("kanaFamilyName", kanaFamilyName);
		model.addAttribute("kanaGivenName", kanaGivenName);
		model.addAttribute("positionList", Profile.POSITION_LIST);
		return "admin/experiences/index";
	}
	
	@GetMapping(value = "/experiences", params = {"univ-loc", "univ-type", "univ-name", "univ-fac", "univ-dep"})
	public String expsIndexFilteredByUniv(@RequestParam(name = "sort", defaultValue = "0") Integer sort, @RequestParam("univ-loc") String univLoc, @RequestParam("univ-name") String univName, @RequestParam("univ-fac") String univFac, @RequestParam("univ-dep") String univDep, Model model) {
		Map<String, Object> result = new HashMap<String, Object>();
		if (!(univDep.isEmpty())) {
			result = experienceService.getExperiencesByDepartment(sort, univLoc, univName, univFac, univDep);
		} else if (!(univFac.isEmpty())) {
			result = experienceService.getExperiencesByFaculty(sort, univLoc, univName, univFac);
		} else if (!(univName.isEmpty())) {
			result = experienceService.getExperiencesByUniversity(sort, univLoc, univName);
		} else if (!(univLoc.isEmpty())) {
			result = experienceService.getExperiencesByPrefecture(sort, univLoc);
		} else {
			return "redirect:/admin/experiences?sort=" + sort;
		}
		model.addAttribute("count", result.get("count"));
		model.addAttribute("experiences", result.get("experiences"));
		model.addAttribute("sort", sort);
		model.addAttribute("positionList", Profile.POSITION_LIST);
		model.addAttribute("hiddenUnivLoc", univLoc);
		model.addAttribute("hiddenUnivName", univName);
		model.addAttribute("hiddenUnivFac", univFac);
		model.addAttribute("hiddenUnivDep", univDep);
		return "admin/experiences/index";
	}
	
	@GetMapping(value = "/experiences", params = {"pos", "cond=and"})
	public String expsIndexFilteredByPosAndPos(@RequestParam(name = "sort", defaultValue = "0") Integer sort, @RequestParam("pos") List<String> positions, Model model) {
		Map<String, Object> result = new HashMap<String, Object>();
		if (!(positions.isEmpty()) && !(positions.get(0).isEmpty())) {
			result = experienceService.getExperiencesByPosition(sort, positions, true);
		} else {
			return "redirect:/admin/experiences?sort=" + sort;
		}
		model.addAttribute("count", result.get("count"));
		model.addAttribute("experiences", result.get("experiences"));
		model.addAttribute("sort", sort);
		model.addAttribute("positions", positions);
		model.addAttribute("positionList", Profile.POSITION_LIST);
		return "admin/experiences/index";
	}
	
	@GetMapping(value = "/experiences", params = {"pos", "cond=or"})
	public String expsIndexFilteredByPosOrPos(@RequestParam(name = "sort", defaultValue = "0") Integer sort, @RequestParam("pos") List<String> positions, Model model) {
		Map<String, Object> result = new HashMap<String, Object>();
		if (!(positions.isEmpty()) && !(positions.get(0).isEmpty())) {
			result = experienceService.getExperiencesByPosition(sort, positions, false);
		} else {
			return "redirect:/admin/experiences?sort=" + sort;
		}
		model.addAttribute("count", result.get("count"));
		model.addAttribute("experiences", result.get("experiences"));
		model.addAttribute("sort", sort);
		model.addAttribute("positions", positions);
		model.addAttribute("positionList", Profile.POSITION_LIST);
		return "admin/experiences/index";
	}
	
	@PostMapping("/experiences")
	public String expsCreate(@Validated ExperienceForm form, BindingResult result, Model model) {
		Experience newExperience = experienceService.convertExperienceFormIntoExperience(form);
		experienceService.register(newExperience);
		return "admin/experiences/create";
	}
	
	@GetMapping("/experiences/new")
	public String expsNew(Model model) {
		ExperienceForm form = new ExperienceForm();
		form.init();
		model.addAttribute(form);
		model.addAttribute("positionList", Profile.POSITION_LIST);
		return "admin/experiences/new";
	}
	
	@PostMapping("/experiences/new")
	public String expsView(@Validated ExperienceForm form, BindingResult result, Model model) {
		form.setClub(this.cleanUp(form.getClub(), ""));
		form.setOffer(this.cleanUp(form.getOffer(), ""));
		form.setEs(this.cleanUp(form.getEs(), new EsForm()));
		form.setInterview(this.cleanUp(form.getInterview(), new InterviewForm()));
		if (result.hasErrors()) {
			model.addAttribute("positionList", Profile.POSITION_LIST);
			model.addAttribute("hiddenUnivLoc", form.getUnivLoc());
			model.addAttribute("hiddenUnivName", form.getUnivName());
			model.addAttribute("hiddenUnivFac", form.getUnivFac());
			model.addAttribute("hiddenUnivDep", form.getUnivDep());
			model.addAttribute("hiddenGradLoc", form.getGradLoc());
			model.addAttribute("hiddenGradName", form.getGradName());
			model.addAttribute("hiddenGradSchool", form.getGradSchool());
			model.addAttribute("hiddenGradDiv", form.getGradDiv());
			return "admin/experiences/new";
		} else {
			model.addAttribute(form);
			return "admin/experiences/view";
		}
	}
	
	@PostMapping(value = "/experiences/new", params = "edit")
	public String expsNewEdit(@Validated ExperienceForm form, BindingResult result, Model model) {
		model.addAttribute("experienceId", "new");
		model.addAttribute("positionList", Profile.POSITION_LIST);
		model.addAttribute("hiddenUnivLoc", form.getUnivLoc());
		model.addAttribute("hiddenUnivName", form.getUnivName());
		model.addAttribute("hiddenUnivFac", form.getUnivFac());
		model.addAttribute("hiddenUnivDep", form.getUnivDep());
		model.addAttribute("hiddenGradLoc", form.getGradLoc());
		model.addAttribute("hiddenGradName", form.getGradName());
		model.addAttribute("hiddenGradSchool", form.getGradSchool());
		model.addAttribute("hiddenGradDiv", form.getGradDiv());
		model.addAttribute(form);
		return "admin/experiences/new";
	}
	
//	unmodified below
	
	@GetMapping(value="/experiences/{expId}")
	public String expsShow(@PathVariable(name="expId") Integer expId, Model model) {
		Experience experience = experienceService.getExperienceById(expId, true, false);
		model.addAttribute("experience", experience);
		return "admin/experiences/show";
	}
	
	@GetMapping(value="/experiences/{experienceId}", params="delete")
	public String getDeleteExperiences(@PathVariable(name="experienceId") String experienceId, Model model) {
		if (experienceId.equals("new")) {
			return "redirect:/admin";
		}
		int id = Integer.parseInt(experienceId);
		experienceService.delete(id);
		return "redirect:/admin/experiences";
	}
	
	@GetMapping(value="/experiences/{experienceId}", params="modify")
	public String getModifyExperiences(@PathVariable(name="experienceId") String experienceId, Model model) {
		if (experienceId.equals("new")) {
			return "redirect:/admin";
		}
		model.addAttribute("positionList", Profile.POSITION_LIST);
		int id = Integer.parseInt(experienceId);
		ExperienceForm form = experienceService.convertExperienceIntoExperienceForm(experienceService.getExperienceById(id, false, false));
		model.addAttribute("hiddenUnivLoc", form.getUnivLoc());
		model.addAttribute("hiddenUnivName", form.getUnivName());
		model.addAttribute("hiddenUnivFac", form.getUnivFac());
		model.addAttribute("hiddenUnivDep", form.getUnivDep());
		model.addAttribute("hiddenGradLoc", form.getGradLoc());
		model.addAttribute("hiddenGradName", form.getGradName());
		model.addAttribute("hiddenGradSchool", form.getGradSchool());
		model.addAttribute("hiddenGradDiv", form.getGradDiv());
		model.addAttribute(form);
		return "admin/experiences/modify";
	}
	
	@PostMapping(value="/experiences/{experienceId}", params="post")
	public String postSubmitExperiences(@PathVariable(name="experienceId") String experienceId, @Validated ExperienceForm form, BindingResult result, Model model) {
		model.addAttribute("experienceId", experienceId);
		form.setClub(this.cleanUp(form.getClub(), ""));
		form.setOffer(this.cleanUp(form.getOffer(), ""));
		if (result.hasErrors()) {
			model.addAttribute("positionList", Profile.POSITION_LIST);
			model.addAttribute("hiddenUnivLoc", form.getUnivLoc());
			model.addAttribute("hiddenUnivName", form.getUnivName());
			model.addAttribute("hiddenUnivFac", form.getUnivFac());
			model.addAttribute("hiddenUnivDep", form.getUnivDep());
			model.addAttribute("hiddenGradLoc", form.getGradLoc());
			model.addAttribute("hiddenGradName", form.getGradName());
			model.addAttribute("hiddenGradSchool", form.getGradSchool());
			model.addAttribute("hiddenGradDiv", form.getGradDiv());
			return "admin/experiences/modify";
		} else {
			int experience_id = Integer.parseInt(experienceId);
			Experience experience = experienceService.convertExperienceFormIntoExperience(form);
			experience.setExperience_id(experience_id);
			experienceService.update(experience);
			return "redirect:/admin/experiences/" + experienceId;
		}
	}
	
	@GetMapping("/experiences/{experienceId}/es/new")
	public String getSubmitEs(@PathVariable(name="experienceId") String experienceId, Model model) {
		model.addAttribute("experienceId", experienceId);
		model.addAttribute("esId", "new");
		model.addAttribute(new EsForm());
		return "admin/experiences/modifyEs";
	}
	
	@GetMapping(value="/experiences/{experienceId}/es/{esId}", params="modify")
	public String getModifyEs(@PathVariable(name="experienceId") String experienceId, @PathVariable(name="esId") String esId, Model model) {
		model.addAttribute("experienceId", experienceId);
		model.addAttribute("esId", esId);
		int experience_id = Integer.parseInt(experienceId);
		int es_id = Integer.parseInt(esId);
		EsForm form = experienceService.convertEsIntoEsForm(experienceService.getEsById(experience_id, es_id));
		model.addAttribute(form);
		return "admin/experiences/modifyEs";
	}
		
	@PostMapping(value="/experiences/{experienceId}/es/{esId}", params="post")
	public String postSubmitEs(@PathVariable(name="experienceId") String experienceId, @PathVariable(name="esId") String esId, EsForm form, BindingResult result, Model model) {
		Es es = experienceService.convertEsFormIntoEs(form);
		int experience_id = Integer.parseInt(experienceId);
		es.setExperience_id(experience_id);
		if (esId.equals("new")) {
			experienceService.registerEs(es);
		} else {
			int es_id = Integer.parseInt(esId);
			es.setEs_id(es_id);
			experienceService.updateEs(es);
		}
		return "redirect:/admin/experiences/" + experienceId;
	}
	
	@GetMapping(value="/experiences/{experienceId}/es/{esId}", params="delete")
	public String getDeleteEs(@PathVariable(name="experienceId") String experienceId, @PathVariable(name="esId") String esId, Model model) {
		int experience_id = Integer.parseInt(experienceId);
		int es_id = Integer.parseInt(esId);
		experienceService.deleteEs(experience_id, es_id);
		return "redirect:/admin/experiences/" + experienceId;
	}
	
	@GetMapping("/experiences/{experienceId}/interview/new")
	public String getSubmitInterview(@PathVariable(name="experienceId") String experienceId, Model model) {
		model.addAttribute("experienceId", experienceId);
		model.addAttribute("interviewId", "new");
		model.addAttribute(new InterviewForm());
		return "admin/experiences/modifyInterview";
	}
	
	@GetMapping(value="/experiences/{experienceId}/interview/{interviewId}", params="modify")
	public String getModifyInterview(@PathVariable(name="experienceId") String experienceId, @PathVariable(name="interviewId") String interviewId, Model model) {
		model.addAttribute("experienceId", experienceId);
		model.addAttribute("interviewId", interviewId);
		int experience_id = Integer.parseInt(experienceId);
		int interview_id = Integer.parseInt(interviewId);
		InterviewForm form = experienceService.convertInterviewIntoInterviewForm(experienceService.getInterviewById(experience_id, interview_id));
		model.addAttribute(form);
		return "admin/experiences/modifyInterview";
	}
		
	@PostMapping(value="/experiences/{experienceId}/interview/{interviewId}", params="post")
	public String postSubmitInterview(@PathVariable(name="experienceId") String experienceId, @PathVariable(name="interviewId") String interviewId, InterviewForm form, BindingResult result, Model model) {
		Interview interview = experienceService.convertInterviewFormIntoInterview(form);
		int experience_id = Integer.parseInt(experienceId);
		interview.setExperience_id(experience_id);
		if (interviewId.equals("new")) {
			experienceService.registerInterview(interview);
		} else {
			int interview_id = Integer.parseInt(interviewId);
			interview.setInterview_id(interview_id);
			experienceService.updateInterview(interview);
		}
		return "redirect:/admin/experiences/" + experienceId;
	}
	
	@GetMapping(value="/experiences/{experienceId}/interview/{interviewId}", params="delete")
	public String getDeleteInterview(@PathVariable(name="experienceId") String experienceId, @PathVariable(name="interviewId") String interviewId, Model model) {
		int experience_id = Integer.parseInt(experienceId);
		int interview_id = Integer.parseInt(interviewId);
		experienceService.deleteInterview(experience_id, interview_id);
		return "redirect:/admin/experiences/" + experienceId;
	}
	
	private <T> List<T> cleanUp(List<T> list, T empty) {
		ListIterator<T> listItr = list.listIterator();
		while (listItr.hasNext()) {
			T item = listItr.next();
			if (item.toString().isEmpty()) {
				listItr.remove();
			}
		}
		if (list.isEmpty()) {
			list.add(empty);
		}
		return list;
	}
		
	@GetMapping(value = "/mail", params = "develop")
	public String mailIndex() {
		return "redirect:/admin";
	}
}
