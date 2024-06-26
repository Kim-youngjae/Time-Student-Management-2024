package com.time.studentmanage.web.teacher;

import com.time.studentmanage.domain.dto.teacher.TeacherRespDto;
import com.time.studentmanage.domain.dto.teacher.TeacherSaveReqDto;
import com.time.studentmanage.domain.dto.teacher.TeacherUpdateReqDto;
import com.time.studentmanage.domain.enums.Position;
import com.time.studentmanage.domain.member.Teacher;
import com.time.studentmanage.service.TeacherService;
import com.time.studentmanage.web.login.SessionConst;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class TeacherController {
    private final TeacherService teacherService;

    //선생 목록 페이지
    @GetMapping("/teacher")
    public String teacherList(Model model, HttpSession session) {
        Object sessionObject = session.getAttribute(SessionConst.LOGIN_MEMBER_SESSION);
        //세션이 없는 경우 or 선생이 아닌 경우 main redirect
        if (sessionObject == null || !sessionObject.getClass().equals(Teacher.class)) {
            return "redirect:/";
        }

        //TEACHER 직급을 가진 경우 main redirect
        Teacher teacher = (Teacher)sessionObject;
        if (teacher.getPosition().equals(Position.TEACHER)) {
            return "redirect:/";
        }


        List<TeacherRespDto> teacherList = teacherService.getTeacherAllList();
        model.addAttribute("teacherList", teacherList);
        return "/teacher/teacher_list";
    }

    //선생 등록 페이지
    @GetMapping("/teacher/create")
    public String createForm(@ModelAttribute("teacherSaveReqDto") TeacherSaveReqDto teacherSaveReqDto) {
        return "/teacher/teacher_create_form";
    }

    //선생 등록 로직
    @PostMapping("/teacher/create")
    public String createTeacher(@Valid @ModelAttribute TeacherSaveReqDto teacherSaveReqDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "/teacher/teacher_create_form";
        }

        teacherService.createTeacher(teacherSaveReqDto);
        return "redirect:/teacher";
    }

    //선생 수정 폼
    @GetMapping("/teacher/edit/{id}")
    public String editTeacherForm(@PathVariable(value = "id") Long id, Model model, HttpSession session) {
        //로그인 한 선생 권한 확인을 위해 변수 선언.
        Teacher loginTeacher = (Teacher) session.getAttribute(SessionConst.LOGIN_MEMBER_SESSION);

        //세션이 없는 경우 or 선생이 아닌 경우 main redirect
        if (loginTeacher == null || !loginTeacher.getClass().equals(Teacher.class)) {
            return "redirect:/";
        }
        TeacherRespDto teacherRespDto = teacherService.getTeacherInfo(id);

        model.addAttribute("teacherRespDto", teacherRespDto);

        //직급에 따라 폼을 별도로 리턴.
        if (loginTeacher.getPosition() == Position.TEACHER) {
            log.info("선생");
            return "/teacher/teacher_edit_form";
        } else {
            log.info("관리자");
            return "/teacher/teacher_edit_form_admin";
        }
    }

    //선생 수정 로직
    @PostMapping("/teacher/edit/{id}")
    public String editTeacher(@PathVariable(value = "id") Long id, @Valid TeacherUpdateReqDto teacherUpdateReqDto, BindingResult bindingResult, HttpSession session) {
        if (bindingResult.hasErrors() || session == null) {
            return "/teacher/teacher_edit_form";
        }
        Teacher teacher = teacherService.updateTeacherInfo(id, teacherUpdateReqDto);
        //수정 후 세션에 저장된 값 변경
        session.setAttribute(SessionConst.LOGIN_MEMBER_SESSION, teacher);
        return "redirect:/teacher/edit/"+id;
    }


}
