/**
 * 
 */
package com.fixit.web.forms;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fixit.core.data.WorkingDay;
import com.fixit.core.data.WorkingHours;
import com.fixit.core.general.PropertyGroup;
import com.fixit.core.general.StoredProperties;
import com.fixit.core.utils.Formatter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * @author 		Kostyantin
 * @createdAt 	2017/05/11 19:02:04 GMT+3
 */
public class FormFieldSchedule {

	private List<WorkingDayView> workingWeek;
	
	public FormFieldSchedule() {
		workingWeek = new ArrayList<>();
	}
	
	public FormFieldSchedule(PropertyGroup propGroup) {
		Type listType = new TypeToken<ArrayList<WorkingDayView>>(){}.getType();
		workingWeek = propGroup.getJsonProperty(StoredProperties.FORMS_DEF_TRADESMAN_SHEDULE,  listType);
	}
	
	public List<WorkingDayView> getWorkingWeek() {
		return workingWeek;
	}

	public void setWorkingWeek(List<WorkingDayView> workingWeek) {
		this.workingWeek = workingWeek;
	}
	
	public boolean isValid() {
		for(WorkingDayView workingDay : workingWeek) {
			if(workingDay.isValid()) {
				return true;
			}
		}
		return false;
	}
	
	public WorkingDay[] toWorkingDays() {
		Set<WorkingDay> workingDays = new HashSet<>();
		for(WorkingDayView workingDay : workingWeek) {
			if(workingDay.isValid()) {
				workingDays.add(workingDay.toWorkingDay());
			}
		}
		return workingDays.toArray(new WorkingDay[workingDays.size()]);
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

	public static class WorkingDayView {
		private int day;
		private WorkingHoursView[] hours;
		
		public WorkingDayView() { }

		public WorkingDayView(int day, WorkingHoursView[] hours) {
			this.day = day;
			this.hours = hours;
		}
		public boolean isValid() {
			for(WorkingHoursView workingHours : hours) {
				if(workingHours.isValid()) {
					return true;
				}
			}
			return false;
		}
		public int getDay() {
			return day;
		}
		public void setDay(int day) {
			this.day = day;
		}
		public WorkingHoursView[] getHours() {
			return hours;
		}
		public void setHours(WorkingHoursView[] workingHours) {
			this.hours = workingHours;
		}
		public WorkingDay toWorkingDay() {
			WorkingHours[] workingHours = new WorkingHours[hours.length];
			for(int i = 0; i < workingHours.length; i++) {
				if(hours[i].isValid()) {
					workingHours[i] = hours[i].toWorkingHours();
				}
			}
			return new WorkingDay(day, workingHours);
		}
	}
	
	public static class WorkingHoursView {
		private String open;
		private String close;
		
		public WorkingHoursView() {};
		
		public WorkingHoursView(String open, String close) {
			this.open = open;
			this.close = close;
		}
		public boolean isValid() {
			return Formatter.is24Hours(open) && Formatter.is24Hours(close);
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
		public WorkingHours toWorkingHours() {
			return new WorkingHours(getWorkingHours(true), getWorkingHours(false));
		}
		
		private double getWorkingHours(boolean forOpen) {
			String[] hoursMinutes = forOpen ? open.split(":") : close.split(":");
			String hours = hoursMinutes[0];
			String minutes = hoursMinutes[1];
			
			String value = hours + "." + Formatter.timeToHundredths(Integer.parseInt(minutes));
			return Double.parseDouble(value);
		}

	}	
}
