/**
 * 
 */
package com.fixit.web.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.io.FilenameUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.fixit.components.registration.TradesmanRegistrationController;
import com.fixit.core.dao.sql.ProfessionDao;
import com.fixit.core.dao.sql.RestClientDao;
import com.fixit.core.dao.sql.StoredPropertyDao;
import com.fixit.core.data.MapAreaType;
import com.fixit.core.data.mongo.Tradesman;
import com.fixit.core.data.sql.TradesmanLead;
import com.fixit.core.general.PropertyGroup.Group;
import com.fixit.core.general.StoredProperties;
import com.fixit.core.logging.FILog;
import com.fixit.core.utils.Constants;
import com.fixit.web.config.MessagesProperties;
import com.fixit.web.forms.TradesmanRegistrationForm;
import com.fixit.web.tlutils.ThymeleafUtilities;
import com.google.gson.reflect.TypeToken;

/**
 * @author 		Kostyantin
 * @createdAt 	2017/04/28 18:02:25 GMT+3
 */

@Controller
@Scope("request")
@RequestMapping(TradesmanRegistrationViewController.END_POINT)
public class TradesmanRegistrationViewController {
	
	final static String END_POINT = "tradesmanRegistration";
	private final MessagesProperties msgs;
	private final ThymeleafUtilities thymeleafUtils;
	
	private final TradesmanRegistrationController tradesmanRegistrant;
	private final ProfessionDao professionDao;
	private final StoredPropertyDao storedPropertyDao;
		
	private final String restClientToken;
	
	private HttpServletRequest httpServletRequest;
	
	@Autowired
	public TradesmanRegistrationViewController(MessagesProperties messageSource, ThymeleafUtilities thymeleafUtilities, TradesmanRegistrationController tradesmanRegistrant, RestClientDao restClientDao, ProfessionDao professionDao, StoredPropertyDao storedPropertyDao, HttpServletRequest httpServletRequest) {
		this.msgs = messageSource;
		this.thymeleafUtils = thymeleafUtilities;
		
		this.tradesmanRegistrant = tradesmanRegistrant;
		this.professionDao = professionDao;
		this.storedPropertyDao = storedPropertyDao;
		
		this.restClientToken = restClientDao.findByName(Constants.VAL_WEB_MODULE).getKey();
		
		this.httpServletRequest = httpServletRequest;
	}
	
	@GetMapping
	public ModelAndView showTradesmanRegistration(@RequestParam("leadId") long leadId, ModelAndView mv) {
		if(leadId > 0) {
			TradesmanLead lead = tradesmanRegistrant.findLead(leadId);
			if(lead != null) {
				if(lead.getTradesmanId() != null) {
					mv.addObject(Constants.ARG_ERROR, msgs.getMsg(MessagesProperties.ALREADY_REGISTERED));
				} else {
					initTradesmanRegistrationModel(mv, new TradesmanRegistrationForm(
							storedPropertyDao.getPropertyGroup(Group.forms), 
							lead.getId()
					));
				}
			} else {
				mv.addObject(Constants.ARG_ERROR, msgs.getMsg(MessagesProperties.LEAD_ID_INVALID));
			}
		} else {
			mv.addObject(Constants.ARG_ERROR, msgs.getMsg(MessagesProperties.LEAD_ID_EMPTY));
		}
		mv.setViewName(Constants.VIEW_TRADESMAN_REGISTRATION);
		
		return mv;
	}

	@PostMapping
	public ModelAndView registerTradesman(@Valid @ModelAttribute("form") TradesmanRegistrationForm form,
			  							  BindingResult bindingResult,
										  ModelAndView mv) {
		FILog.i(form.toString());
		
		boolean hasErrors = bindingResult.hasErrors();
		if(!hasErrors) {
			boolean validLogo = form.isValidLogo();
			boolean validFeature = form.isValidFeature();
			boolean validSchedule = form.isValidSchedule();
			if(validLogo && validFeature && validSchedule) {
				Tradesman tradesman = form.toTradesman();
				
				MultipartFile logo = form.getLogo();
				String logoExtension = "." + FilenameUtils.getExtension(logo.getOriginalFilename());
				
				try(InputStream logoInputStream = logo.getInputStream()) {
					tradesmanRegistrant.registerTradesman(form.getLeadId(), tradesman,
							logoInputStream, logoExtension
					);
				} catch (IOException e) {
					FILog.e(Constants.LT_TRADESMAN_REGISTRATION, "Error while registering tradesman " + tradesman.toString(), e, true);
					mv.addObject(Constants.ARG_ERROR, "Illegal logo or feature image.");
					hasErrors = true;
				}
				
				ObjectId tradesmanId = tradesman.get_id();
				if(tradesmanId == null) {
					mv.addObject(Constants.ARG_ERROR, msgs.getMsg(MessagesProperties.ERROR_UNEXPECTED));
					hasErrors = true;
				}
			} else {
				if(!validLogo) {
					bindingResult.addError(new FieldError(Constants.ARG_FORM, "logo", msgs.getMsg(MessagesProperties.LOGO_EMPTY)));
				}
				if(!validFeature) {
					bindingResult.addError(new FieldError(Constants.ARG_FORM, "featureImage", msgs.getMsg(MessagesProperties.FEATURE_IMAGE_EMPTY)));
				}
				if(!validSchedule) {
					bindingResult.addError(new FieldError(Constants.ARG_FORM, "schedule", msgs.getMsg(MessagesProperties.SCHEDULE_INVALID)));
				}
				hasErrors = true;
			}
		} 
		
		if(hasErrors) {
			initTradesmanRegistrationModel(mv, form);	
		} else {
			mv.addObject(Constants.ARG_SUCCESS_MESSAGE, msgs.getMsg(MessagesProperties.TRADESMAN_REGISTRATION_SUCCESS));
		}
		
		mv.setViewName(Constants.VIEW_TRADESMAN_REGISTRATION);
		return mv;
	}
	
	private void initTradesmanRegistrationModel(ModelAndView mv, TradesmanRegistrationForm form) {
		mv.addObject(Constants.ARG_FORM, form);
		String apiUrl = httpServletRequest.getScheme() + "://" + httpServletRequest.getServerName() + "/rest";
		mv.addObject(Constants.ARG_BASE_API_URL, apiUrl);
		mv.addObject(Constants.ARG_AUTHORIZATION_TOKEN, restClientToken);
		mv.addObject(Constants.ARG_INITIAL_MAP_AREA_TYPE, MapAreaType.Province.name());
		mv.addObject(Constants.ARG_PROFESSIONS, professionDao.getActiveProfessions());
		Type listType = new TypeToken<ArrayList<String>>(){}.getType();
		mv.addObject(Constants.ARG_FEATURE_IMAGES, storedPropertyDao.getPropertyGroup(Group.web).getJsonProperty(StoredProperties.WEB_FEATURE_IMAGE, listType));
		mv.addObject("calUtils", thymeleafUtils.getCalendarUtils());
	}
	
}
