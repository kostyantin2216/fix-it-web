/**
 * 
 */
package com.fixit.web.forms;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.fixit.core.general.PropertyGroup;
import com.fixit.core.general.StoredProperties;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * @author 		Kostyantin
 * @createdAt 	2017/05/11 19:02:04 GMT+3
 */
public class FormFieldSchedule {

	private List<WorkingDay> workingWeek;
	
	public FormFieldSchedule() {
		workingWeek = new ArrayList<>();
	}
	
	public FormFieldSchedule(PropertyGroup propGroup) {
		Type listType = new TypeToken<ArrayList<WorkingDay>>(){}.getType();
		workingWeek = propGroup.getJsonProperty(StoredProperties.FORMS_DEF_TRADESMAN_SHEDULE,  listType);
	}
	
	public List<WorkingDay> getWorkingWeek() {
		return workingWeek;
	}

	public void setWorkingWeek(List<WorkingDay> workingWeek) {
		this.workingWeek = workingWeek;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

	public static class WorkingDay {
		private int day;
		private WorkingHours[] hours;
		
		public WorkingDay() { }
		
		public WorkingDay(int day, WorkingHours[] hours) {
			this.day = day;
			this.hours = hours;
		}
		
		public int getDay() {
			return day;
		}
		public void setDay(int day) {
			this.day = day;
		}
		public WorkingHours[] getHours() {
			return hours;
		}
		public void setHours(WorkingHours[] workingHours) {
			this.hours = workingHours;
		}
	}
	
	public static class WorkingHours {
		private String open;
		private String close;
		
		public WorkingHours() {};
		
		public WorkingHours(String open, String close) {
			this.open = open;
			this.close = close;
		}
		public String getOpen() {
			return open;
		}
		public void setOpen(String open) {
			this.open = open;
		}
		public String getClose() {
			return close;
		}
		public void setClose(String closed) {
			this.close = closed;
		}
	}	
}
