package jp.co.axiz.web.controller;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jp.co.axiz.web.entity.SessionInfo;
import jp.co.axiz.web.entity.UserInfo;
import jp.co.axiz.web.form.UpdateForm;
import jp.co.axiz.web.service.impl.UserInfoService;

@Controller
public class UpdateController {

	@Autowired
	private SessionInfo sessionInfo;

	@Autowired
    MessageSource messageSource;

	@Autowired
	private UserInfoService userInfoService;

	@RequestMapping("/update")
	public String update(@ModelAttribute("updateForm") UpdateForm form, Model model) {
		return "update";
	}

	@RequestMapping(value = "/updateInput", method = RequestMethod.POST)
	public String updateInput(@Validated @ModelAttribute("updateForm") UpdateForm form, BindingResult bindingResult,
			Model model) {

		if (bindingResult.hasFieldErrors("userId")) {
			String errorMsg = messageSource.getMessage("required.error", null, Locale.getDefault());
			model.addAttribute("errmsg", errorMsg);
			return "update";
		}

		UserInfo user = userInfoService.findById(form.getUserId());

		if(user == null) {//IDが空であればupdateへ
			String errorMsg = messageSource.getMessage("id.not.found.error", null, Locale.getDefault());
			model.addAttribute("errmsg", errorMsg);
			return "update";
		}

		//user型でsessionに保存
		sessionInfo.setPrevUser(user);

		//変更された項目
		form.setNewName(user.getUserName());
		form.setNewTel(user.getTelephone());
		form.setNewPassword(user.getPassword());

		return "updateInput"; //更新内容入力画面へ
	}

	@RequestMapping(value = "/updateConfirm", method = RequestMethod.POST)
	public String updateConfirm(@Validated @ModelAttribute("updateForm") UpdateForm form, BindingResult bindingResult,
			Model model) {

		if (form.hasRequiredError()) {
			String errorMsg = messageSource.getMessage("required.error", null, Locale.getDefault());
			model.addAttribute("errmsg", errorMsg);
			return "updateInput"; //更新内容入力画面へ
		}

		UserInfo beforeUser = sessionInfo.getPrevUser();

		UserInfo afterUser = new UserInfo();
		afterUser.setUserId(beforeUser.getUserId());
		afterUser.setUserName(form.getNewName());
		afterUser.setTelephone(form.getNewTel());
		afterUser.setPassword(form.getNewPassword());

		if(afterUser.equals(beforeUser)) {
			String errorMsg = messageSource.getMessage("required.change", null, Locale.getDefault());
			model.addAttribute("errmsg", errorMsg);
			return "updateInput";//更新内容入力画面へ
		}

		sessionInfo.setAfterUser(afterUser);

		form.setPrevName(beforeUser.getUserName());
		form.setPrevTel(beforeUser.getTelephone());
		form.setPrevPassword(beforeUser.getPassword());

		if(beforeUser.getPassword().equals(afterUser.getPassword())) {//passwordが変わっていたら
			form.setConfirmNewPassword(afterUser.getPassword());
		}

		return "updateConfirm";//更新内容確認画面へ
	}

	@RequestMapping(value = "/updateInputBack")
	public String updateInputBack(@ModelAttribute("updateForm") UpdateForm form, Model model) {

		UserInfo afterUser = sessionInfo.getAfterUser();

		form.setUserId(afterUser.getUserId());
		form.setNewName(afterUser.getUserName());
		form.setNewTel(afterUser.getTelephone());
		form.setNewPassword(afterUser.getPassword());

		return "updateInput";//更新内容入力画面へ
	}


	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public String updateExecute(@Validated @ModelAttribute("updateForm") UpdateForm form, BindingResult bindingResult,
			Model model) {

		UserInfo afterUser = sessionInfo.getAfterUser();

		if(!afterUser.getPassword().equals(form.getConfirmNewPassword())) {//passwordが一致しなければ
			String errorMsg = messageSource.getMessage("password.not.match.error", null, Locale.getDefault());
			model.addAttribute("errmsg", errorMsg);

			form.setConfirmNewPassword("");

			UserInfo beforeUser = sessionInfo.getPrevUser();
			form.setPrevName(beforeUser.getUserName());
			form.setPrevTel(beforeUser.getTelephone());
			form.setPrevPassword(beforeUser.getPassword());

			return "updateConfirm";//更新内容確認画面へ
		}

		userInfoService.update(afterUser);

		sessionInfo.setAfterUser(null);
		sessionInfo.setPrevUser(null);

		model.addAttribute("user", sessionInfo.getLoginUser());

		return "updateResult";//更新結果画面へ
	}

}
