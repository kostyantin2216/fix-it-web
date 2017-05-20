/**
 * 
 */
package com.fixit.web.controllers;

import java.io.IOException;
import java.io.InputStream;

import javax.validation.Valid;

import org.apache.commons.io.FilenameUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.fixit.components.registration.tradesmen.TradesmanRegistrant;
import com.fixit.core.dao.sql.ProfessionDao;
import com.fixit.core.dao.sql.RestClientDao;
import com.fixit.core.dao.sql.StoredPropertyDao;
import com.fixit.core.data.MapAreaType;
import com.fixit.core.data.mongo.Tradesman;
import com.fixit.core.data.sql.TradesmanLead;
import com.fixit.core.general.FileManager;
import com.fixit.core.general.PropertyGroup.Group;
import com.fixit.core.logging.FILog;
import com.fixit.core.utils.Constants;
import com.fixit.web.config.MessagesProperties;
import com.fixit.web.forms.TradesmanRegistrationForm;
import com.fixit.web.tlutils.ThymeleafUtilities;

/**
 * @author 		Kostyantin
 * @createdAt 	2017/04/28 18:02:25 GMT+3
 */

@Controller
@Scope("session")
@RequestMapping(TradesmanRegistrationController.END_POINT)
public class TradesmanRegistrationController {
	
	final static String END_POINT = "tradesmanRegistration";
	
	private final FileManager fileManager;
	private final MessagesProperties msgs;
	
	private final TradesmanRegistrant tradesmanRegistrant;
	private final ProfessionDao professionDao;
	private final StoredPropertyDao storedPropertyDao;
		
	private final String restClientToken;
	
	@Autowired
	public TradesmanRegistrationController(FileManager fileManager, MessagesProperties messageSource, TradesmanRegistrant tradesmanRegistrant, RestClientDao restClientDao, ProfessionDao professionDao, StoredPropertyDao storedPropertyDao) {
		this.fileManager = fileManager;
		this.msgs = messageSource;
		
		this.tradesmanRegistrant = tradesmanRegistrant;
		this.professionDao = professionDao;
		this.storedPropertyDao = storedPropertyDao;
		
		this.restClientToken = restClientDao.findByName(Constants.VAL_WEB_MODULE).getKey();
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
	public ModelAndView registerTradesman(
										  @Valid @ModelAttribute("form") TradesmanRegistrationForm form,
			  							  BindingResult bindingResult,
										  ModelAndView mv) {
		FILog.i(form.toString());
		
		if(!bindingResult.hasErrors()) {
			if(form.isValidLogo()) {
				Tradesman tradesman = form.toTradesman();
				tradesmanRegistrant.registerTradesman(tradesman);
				
				ObjectId tradesmanId = tradesman.get_id();
				if(tradesmanId != null) {
					mv.addObject(Constants.ARG_ERROR, "success");
				} else {
					mv.addObject(Constants.ARG_ERROR, msgs.getMsg(MessagesProperties.ERROR_UNEXPECTED));
				}
			} else {
				bindingResult.addError(new FieldError(Constants.ARG_FORM, "logo", msgs.getMsg(MessagesProperties.LOGO_EMPTY)));
			}
		} else {		
			initTradesmanRegistrationModel(mv, form);
			mv.setViewName(Constants.VIEW_TRADESMAN_REGISTRATION);
		}
		return mv;
	}
	
	private void initTradesmanRegistrationModel(ModelAndView mv, TradesmanRegistrationForm form) {
		mv.addObject(Constants.ARG_FORM, form);
		mv.addObject(Constants.ARG_BASE_API_URL, "http://127.0.0.1/rest");
		mv.addObject(Constants.ARG_AUTHORIZATION_TOKEN, restClientToken);
		mv.addObject(Constants.ARG_INITIAL_MAP_AREA_TYPE, MapAreaType.Province.name());
		mv.addObject(Constants.ARG_PROFESSIONS, professionDao.getActiveProfessions());
		mv.addObject("calUtils", ThymeleafUtilities.getCalendarUtils());
	}
	
	public String editTradesmanFeatures(@RequestParam("logo") MultipartFile logo,
										@RequestParam("feature") MultipartFile feature,
										ModelAndView mv,
										BindingResult bindingResult) {
	//	mv.setViewName("redirect:" + END_POINT + "?leadId=" + leadId);
		if(!logo.isEmpty()) {
			InputStream logoInputStream= null;
			try {				
				logoInputStream = logo.getInputStream();
				String fileExtension = "." + FilenameUtils.getExtension(logo.getOriginalFilename());
				fileManager.storeTradesmanLogo("21edre432f", fileExtension, logoInputStream);
				throw new IllegalStateException();
			
			} catch (IllegalStateException | IOException e) {
				bindingResult.addError(new ObjectError("logo", "invalid logo"));
				//mv.addObject(Constants.ARG_ERROR, Throwables.getStackTraceAsString(e));
			} finally {
				if(logoInputStream != null) {
					try {
						logoInputStream.close();
					} catch (IOException e) {
						FILog.e("registration error", e);
					}
				}
			}
		}
		//
		mv.addObject(bindingResult);
		mv.setViewName(Constants.VIEW_TRADESMAN_REGISTRATION);
		return null;
	}
	
}
