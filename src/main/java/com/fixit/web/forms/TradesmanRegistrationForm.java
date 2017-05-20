package com.fixit.web.forms;

import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;

import com.fixit.core.data.mongo.Tradesman;
import com.fixit.core.general.PropertyGroup;
import com.fixit.web.forms.validation.Telephone;

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
	
	@NotNull(message = "{validation.profession.empty}")
	@Min(value = 1, message = "{validation.profession.empty}")
	private Integer profession;
	
	private MultipartFile logo;
	
	private MultipartFile feature;
	
	public TradesmanRegistrationForm() { }
	
	public TradesmanRegistrationForm(PropertyGroup propertyGroup, Long leadId) {
		this.leadId = leadId;
		this.schedule = new FormFieldSchedule(propertyGroup);
	}
	
	public boolean isValidLogo() {
		return logo != null && !logo.isEmpty();
	}
	
	public boolean isValidFeature() {
		return feature != null && !feature.isEmpty();
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
	
	public Integer getProfession() {
		return profession;
	}
	
	public void setProfession(Integer profession) {
		this.profession = profession;
	}
	
	public MultipartFile getLogo() {
		return logo;
	}

	public void setLogo(MultipartFile logo) {
		this.logo = logo;
	}

	public MultipartFile getFeature() {
		return feature;
	}

	public void setFeature(MultipartFile feature) {
		this.feature = feature;
	}

	public Tradesman toTradesman() {
		Tradesman tradesman = new Tradesman();
		tradesman.setCompanyName(companyName);
		tradesman.setContactName(contactName);
		tradesman.setEmail(email);
		tradesman.setProfessionId(profession);
		tradesman.setTelephone(telephone);
		tradesman.setRating(0.0f);
		
		ObjectId[] workingAreasIds = new ObjectId[workingAreas.size()];
		for(int i = 0; i < workingAreasIds.length; i++) {
			workingAreasIds[i] = new ObjectId(workingAreas.get(i));
		}
		tradesman.setWorkingAreas(workingAreasIds);
		
		return tradesman;
	}

	@Override
	public String toString() {
		return "TradesmanRegistrationForm [leadId=" + leadId + ", contactName=" + contactName + ", email=" + email
				+ ", telephone=" + telephone + ", workingAreas=" + workingAreas + ", schedule=" + schedule
				+ ", companyName=" + companyName + ", profession=" + profession + ", logo=" + logo + ", feature="
				+ feature + "]";
	}

}
