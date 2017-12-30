package com.fixit.web.forms;

import java.util.List;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;

import com.fixit.core.data.mongo.Tradesman;
import com.fixit.core.general.PropertyGroup;
import com.fixit.web.forms.validation.Telephone;
import com.google.common.primitives.Ints;

/**
 * @author 		Kostyantin
 * @createdAt 	2017/05/05 22:26:39 GMT+3
 */
public class TradesmanRegistrationForm {

	private Long leadId;
	
	@NotEmpty(message = "{validation.contact.name.empty}")
	private String contactName;
	
	@Email(message = "{validation.email.invalid}")
	@NotEmpty(message = "{validation.email.empty}")
	private String email;

	@Telephone(message= "{validation.telephone.invalid}")
	private String telephone;
	
	@Size(min = 1, message = "{validation.working.areas.empty}")
	private List<String> workingAreas;
	
	private FormFieldSchedule schedule;
	
	@NotEmpty(message = "{validation.company.name.empty}")
	private String companyName;
	
	@Size(min = 1, message = "{validation.profession.empty}")
	private List<Integer> professions;
	
	private MultipartFile logo;
	
	@NotEmpty(message = "{validation.feature.image.empty}")
	private String featureImage;
	
	public TradesmanRegistrationForm() { }
	
	public TradesmanRegistrationForm(PropertyGroup propertyGroup, Long leadId) {
		this.leadId = leadId;
		this.schedule = new FormFieldSchedule(propertyGroup);
	}
	
	public boolean isValidLogo() {
		return logo != null && !logo.isEmpty();
	}
	
	public boolean isValidFeature() {
		return featureImage != null && !featureImage.isEmpty();
	}
	
	public boolean isValidSchedule() {
		return schedule.isValid();
	}
	
	public Long getLeadId() {
		return leadId;
	}
	
	public void setLeadId(Long leadId) {
		this.leadId = leadId;
	}
	
	public String getContactName() {
		return contactName;
	}
	
	public void setContactName(String contactName) {
		this.contactName = contactName;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getTelephone() {
		return telephone;
	}
	
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	
	public List<String> getWorkingAreas() {
		return workingAreas;
	}

	public void setWorkingAreas(List<String> workingAreas) {
		this.workingAreas = workingAreas;
	}	

	public FormFieldSchedule getSchedule() {
		return schedule;
	}

	public void setSchedule(FormFieldSchedule schedule) {
		this.schedule = schedule;
	}

	public String getCompanyName() {
		return companyName;
	}
	
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	
	public List<Integer> getProfessions() {
		return professions;
	}
	
	public void setProfessions(List<Integer> professions) {
		this.professions = professions;
	}
	
	public MultipartFile getLogo() {
		return logo;
	}

	public void setLogo(MultipartFile logo) {
		this.logo = logo;
	}

	public String getFeatureImage() {
		return featureImage;
	}

	public void setFeatureImage(String featureImage) {
		this.featureImage = featureImage;
	}

	public Tradesman toTradesman() {
		Tradesman tradesman = new Tradesman();
		tradesman.setLeadId(leadId);
		tradesman.setCompanyName(companyName);
		tradesman.setContactName(contactName);
		tradesman.setEmail(email);
		tradesman.setProfessions(Ints.toArray(professions));
		tradesman.setTelephone(telephone);
		tradesman.setRating(0.0f);
		tradesman.setWorkingDays(schedule.toWorkingDays());
		tradesman.setFeatureImageUrl(featureImage);
		tradesman.setWorkingAreas(workingAreas.toArray(new String[workingAreas.size()]));
		
		return tradesman;
	}

	@Override
	public String toString() {
		return "TradesmanRegistrationForm [leadId=" + leadId + ", contactName=" + contactName + ", email=" + email
				+ ", telephone=" + telephone + ", workingAreas=" + workingAreas + ", schedule=" + schedule
				+ ", companyName=" + companyName + ", professions=" + professions + ", logo=" + logo + ", feature="
				+ featureImage + "]";
	}

}
