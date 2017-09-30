"use strict";

// Polyfill's
// ==========================

if (!Array.prototype.filter) {
	Array.prototype.filter = function(fun/*, thisArg*/) {

		if (this === void 0 || this === null) {
			throw new TypeError();
		}

		var t = Object(this);
		var len = t.length >>> 0;
		if (typeof fun !== 'function') {
			throw new TypeError();
		}

		var res = [];
		var thisArg = arguments.length >= 2 ? arguments[1] : void 0;
		for (var i = 0; i < len; i++) {
			if (i in t) {
				var val = t[i];

				// NOTE: Technically this should Object.defineProperty at
				//       the next index, as push can be affected by
				//       properties on Object.prototype and Array.prototype.
				//       But that method's new, and collisions should be
				//       rare, so use the more-compatible alternative.
				if (fun.call(thisArg, val, i, t)) {
					res.push(val);
				}
			}
		}

		return res;
	};
}
if (!Array.prototype.forEach) {

	Array.prototype.forEach = function(callback/*, thisArg*/) {

		var T, k;

		if (this == null) {
			throw new TypeError('this is null or not defined');
		}

		// 1. Let O be the result of calling toObject() passing the
		// |this| value as the argument.
		var O = Object(this);

		// 2. Let lenValue be the result of calling the Get() internal
		// method of O with the argument "length".
		// 3. Let len be toUint32(lenValue).
		var len = O.length >>> 0;

		// 4. If isCallable(callback) is false, throw a TypeError exception. 
		// See: http://es5.github.com/#x9.11
		if (typeof callback !== 'function') {
			throw new TypeError(callback + ' is not a function');
		}

		// 5. If thisArg was supplied, let T be thisArg; else let
		// T be undefined.
		if (arguments.length > 1) {
			T = arguments[1];
		}

		// 6. Let k be 0
		k = 0;

		// 7. Repeat, while k < len
		while (k < len) {

			var kValue;

			// a. Let Pk be ToString(k).
			//    This is implicit for LHS operands of the in operator
			// b. Let kPresent be the result of calling the HasProperty
			//    internal method of O with argument Pk.
			//    This step can be combined with c
			// c. If kPresent is true, then
			if (k in O) {

				// i. Let kValue be the result of calling the Get internal
				// method of O with argument Pk.
				kValue = O[k];

				// ii. Call the Call internal method of callback with T as
				// the this value and argument list containing kValue, k, and O.
				callback.call(T, kValue, k, O);
			}
			// d. Increase k by 1.
			k++;
		}
		// 8. return undefined
	};
}

// Polyfill's End
//==========================


function ThymeleafScript(baseApiUrl, authorizationToken, initialMapAreaType) {

	// Script Global Variable
	// ======================================

	var mapWrapper = null;
	var mapControl = null;
	var mapSelections = null;
	var mapSelectionsCount = null;
	var apiAccess = new ApiAccess(baseApiUrl, authorizationToken);
	var steps = new RegistrationSteps([
		new Step('contact-details', [
			/*new Validator('contactName', function(value) {
				return value.length > 0;
			}),
			new Validator('email', function(value) {
				return validateEmail(value);
			}),
			new Validator('telephone', function(value) {
				return telephone.intlTelInput("isValidNumber");
			})*/
		]),
		new Step('company-details', [
			/*new Validator('companyName', function(value) {
				return value.length > 0;
			})*/
		]),
		new Step('area-details', [], function() {mapWrapper.fitCenter;}),
		new Step('feature-details', [])
	]);


	// Functions
	// ======================================

	// public functions
	
	this.initMap = function() {
		mapSelections = new MapSelections($("#workingAreas").val());
		mapSelectionsCount = new MapSelectionsCount($('#areaCount'), mapSelections.count());

		var johannesburg = { lat: -26.171, lng: 28.110 };
		mapWrapper = new MapWrapper(document.getElementById('map'),  johannesburg);
		mapWrapper.registerSelectionListener(mapSelections);
		mapWrapper.registerSelectionListener(mapSelectionsCount);

		mapControl = new MapControl(mapWrapper);

		apiAccess.getMapAreas(initialMapAreaType, mapWrapper.showAreas);
	}
	
	this.prepareForm = function() {
		$("#workingAreas").val(mapSelections.toInputValue());
		
		var telephoneInput = $("#telephone");
		telephoneInput.val(telephoneInput.intlTelInput("getNumber"));
	}

	// private functions
	
	function validateEmail(email) {
	  var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
	  return re.test(email);
	}

	// Classes
	// =======================================
	
	function Step(elementId, validators, selectionCallback) {
		this.elementId = elementId;
		this.validators = validators;
		this.selectionCallback = selectionCallback;
		
		this.validate = function() {
			var valid = true;
			for(var i = 0; i < this.validators.length; i++){
				if(!this.validators[i].validate()) {
					valid = false;
				}
			}
			return valid;
		};
	}
	
	function Validator(inputElementId, inputValidator) {
		this.inputElementId = inputElementId;
		this.inputValidator = inputValidator;
		
		this.validate = function() {
			var inputElement = $('#' + this.inputElementId);
			if(inputValidator(inputElement.val())) {
				inputElement.removeClass('has-error');
				return true;
			} else {
				inputElement.addClass('has-error');
				return false;
			}
		};
	}
	
	function RegistrationSteps(steps) {	
		var steps = steps;
		var currentStepIndex;
		
		setStep(0);
		
		$('#next').click(onNext);
		$('#prev').click(onPrev);
		$('#submit').css("display", "none");
		
		function setStep(stepIndex, validate) {
			var step = steps[stepIndex];
			if(step && (validate ? step.validate() : true)) {
				currentStepIndex = stepIndex;
				
				for(var i = 0; i < steps.length; i++) {
					$("#" + steps[i].elementId).css('display', (i == stepIndex ? 'block' : 'none'));
				}			
			
				if(stepIndex == 0) {
					$("#next").css("display", "inline-block");
					$("#prev").css("display", "none");
				} else if(stepIndex == (steps.length - 1)) {
					$("#next").css("display", "none");
					$("#prev").css("display", "inline-block");
					$('#submit').css("display", "inline-block");
				} else {
					$("#next").css("display", "inline-block");
					$("#prev").css("display", "inline-block");
				}

				if(step.selectionCallback) {
					step.selectionCallback();
				}
			}
		}
		
		function onNext() {
			var newStep = currentStepIndex + 1;
			if(newStep < steps.length) {
				setStep(newStep, true);
			}
		}
		
		function onPrev() {
			var newStep = currentStepIndex - 1;
			if(newStep >= 0) {
				setStep(newStep, true);
			}
		}
	}

	function MapSelections(inputValue) {
		var selectedAreas = [];

		if(inputValue) {
			selectedAreas = inputValue.split(',');
		}
		
		return {
			get: function() { selectedAreas },
			contains: function(areaId) {
				return selectedAreas.indexOf(areaId) > -1;
			}, 
			onAreaSelected: function(areaId) {
				selectedAreas.push(areaId);
			},
			onAreaUnselected: function(areaId) {
				selectedAreas = selectedAreas.filter(function(item) {
					return item != areaId;
				});
			},
			onAreasCleared: function() {
				selectedAreas = [];
			},
			toInputValue: function() {
				var result = "";
				for(var i = 0; i < selectedAreas.length; i++) {
					var areaId = selectedAreas[i];
					if(i > 0) {
						result += ",";
					}
					result += areaId;
				}
				return result;
			},
			count: function() {
				return selectedAreas.length;
			}
		};
	}


	

	function MapSelectionsCount(countDiv, count) {
		update();
		
		function update() {
			if(count == 0) {
				countDiv.text("You haven't selected any areas");
				countDiv.removeClass('alert-success');
				countDiv.addClass('alert-warning');
			} else {
				countDiv.text("You have selected " + count + " areas");
				countDiv.removeClass('alert-warning');
				countDiv.addClass('alert-success');
			}
		}
		
		return {
			onAreaSelected: function(areaId) {
				count++;
				update();
			},
			onAreaUnselected: function(areaId) {
				count--;
				update();
			},
			onAreasCleared: function() {
				count = 0;
				update();
			}
		};
	}

	

	function MapWrapper(mapElem, center) {
		const DEFAULT_ZOOM = 8;

		const SELECTED_POLYGON_COLOR = '#11ff00';
		const UNSELECTED_POLYGON_COLOR = '#ff0000';

		const LOWEST_AREA_TYPE = 'Ward';
		const HIGHEST_AREA_TYPE = 'Province';

		const EVENT_ON_AREA_SELECTED = 'onAreaSelected';
		const EVENT_ON_AREA_UNSELECTED = 'onAreaUnselected';
		const EVENT_ON_AREAS_CLEARED = 'onAreasCleared';

		// Public Variables
		this.map = new google.maps.Map(mapElem, {
			zoom: DEFAULT_ZOOM,
			center: center
		});

		// Private Variables
		var self = this;
		var parentAreaIdStack = [];
		var polygons = [];
		var bounds = {};
		var observers = {};

		// Public Methods
		this.showAreas = function(areas) {
			clearPolygons();
			bounds = new google.maps.LatLngBounds();

			for(var i = 0; i < areas.length; i++) {
				var area = areas[i];
				var coordinates = area.geometry.coordinates[0];
				var paths = [];

				for(var j = 0; j < coordinates.length; j++) {
					var point = new google.maps.LatLng(coordinates[j][1], coordinates[j][0]);
					bounds.extend(point);
					paths.push(point);
				}

				var isSelected = mapSelections.contains(area._id);		
				var color = isSelected ? SELECTED_POLYGON_COLOR : UNSELECTED_POLYGON_COLOR;
				var areaPolygon = new google.maps.Polygon({
					areaId: area._id,
					parentId: area.parentId,
					hasChildren: area.type != LOWEST_AREA_TYPE,
					isSelected: isSelected,
					paths: paths,
					strokeColor: color,
					strokeOpacity: 0.75,
					strokeWeight: 2,
					fillColor: color,
					fillOpacity: 0.1
				});
				areaPolygon.setMap(self.map);
				polygons.push(areaPolygon);
				google.maps.event.addListener(areaPolygon, 'click', function() {
					onAreaSelected(this);
				});
			}

			self.fitCenter();
		}

		this.showParentAreaTypes = function() {
			if(parentAreaIdStack.length) {
				apiAccess.getChildMapAreas(parentAreaIdStack.pop(), self.showAreas);
			} else {
				apiAccess.getMapAreas(initialMapAreaType, mapWrapper.showAreas);
			}
		}

		this.unselectPolygons = function() {
			for(var i = 0; i < polygons.length; i++) {
				polygons[i].isSelected = false;
				polygons[i].setOptions({
					strokeColor: UNSELECTED_POLYGON_COLOR,
					fillColor: UNSELECTED_POLYGON_COLOR
				});
			}
			notifyObservers(EVENT_ON_AREAS_CLEARED);
		}

		this.fitCenter = function() {
			self.map.fitBounds(bounds);
			self.map.setZoom(self.map.getZoom() + 1);
			google.maps.event.trigger(self.map, 'resize');
		}
		
		this.registerSelectionListener = function(listener) {
			registerObserver([EVENT_ON_AREA_SELECTED, EVENT_ON_AREA_UNSELECTED, EVENT_ON_AREAS_CLEARED], listener);
		}

		// Private Methods
		function clearPolygons() {
			while(polygons.length) {
				polygons.pop().setMap(null);
			}
		}

		function onAreaSelected(polygon) {
			if(polygon.hasChildren) {
				if(polygon.parentId) {
					parentAreaIdStack.push(polygon.parentId);
				}
				apiAccess.getChildMapAreas(polygon.areaId, self.showAreas);
			} else {
				polygon.isSelected = !polygon.isSelected;

				var newColor = polygon.isSelected ? SELECTED_POLYGON_COLOR : UNSELECTED_POLYGON_COLOR;
				polygon.setOptions({
					strokeColor: newColor,
					fillColor: newColor
				});

				notifySelection(polygon);
			}
		}

		function notifySelection(polygon) {
			var event = polygon.isSelected ? EVENT_ON_AREA_SELECTED : EVENT_ON_AREA_UNSELECTED;
			if(observers[event]) {
				observers[event].forEach(function(observer) {
					observer(polygon.areaId);
				});
			}
		}

		function notifyObservers(event) {
			if(observers[event]) {
				observers[event].forEach(function(observer) {
					observer();
				});
			}
		}

		function registerObserver(events, observer) {
			events.forEach(function(event) {
				if(observer[event]) {
					if(!observers[event]) {
						observers[event] = [];
					}
					observers[event].push(observer[event]);
				}
			});
		}
	}
	
	
	


	function MapControl(mapWrapper) {
		var controlDiv = document.createElement('div');
		controlDiv.id = 'map-control'; 
		controlDiv.className = 'map-control'; 

		controlDiv.appendChild(createBackControl());
		controlDiv.appendChild(createClearSelectionsControl());
		controlDiv.appendChild(createCenterMapControl());

		controlDiv.index = 1;
		mapWrapper.map.controls[google.maps.ControlPosition.TOP_CENTER].push(controlDiv);

		// Private Methods
		function createBackControl() {
			var backControl = createControl('Back', 'Click to go up a level');

			backControl.addEventListener('click', function() {
				mapWrapper.showParentAreaTypes();
			});

			return backControl;
		}

		function createClearSelectionsControl() {
			var clearSelectionsControl = createControl('Clear Selections', 'Click to clear all selections.');

			clearSelectionsControl.addEventListener('click', function() {
				mapWrapper.unselectPolygons();
			});

			return clearSelectionsControl;
		}

		function createCenterMapControl() {
			var centerMapControl = createControl('Center Map', 'Click to recenter the map');

			centerMapControl.addEventListener('click', function() {
				mapWrapper.fitCenter();
			});

			return centerMapControl;
		}

		function createControl(text, hint) {
			var controlContainer = document.createElement('div');
			controlContainer.className = 'map-control-container';
			controlContainer.title = hint;

			var controlText = document.createElement('div');
			controlText.className = 'map-control-interior';
			controlText.innerHTML = text;
			controlContainer.appendChild(controlText);

			return controlContainer;
		}

	}




	function ApiAccess(baseApiUrl, authorizationToken) {	
		var headers = {
				'X-Authorization': authorizationToken
		};
		var mapAreaApi = new MapAreaApi();

		this.getMapAreas = function(type, callback) {
			mapAreaApi.getAreasForType(type, callback);
		}

		this.getParentsForType = function(type, callback) {
			mapAreaApi.getParentAreasForType(type, callback);
		}

		this.getChildMapAreas = function(parentId, callback) {
			mapAreaApi.getChildren(parentId, callback);
		}
		function MapAreaApi() {
			this.endPoint = '/data/MapAreas';

			this.getAreasForType = function(type, callback) {
				$.get({
					url: baseApiUrl + this.endPoint + "/ofType/" + type,
					headers: headers
				}).done(callback);
			}

			this.getParentAreasForType = function(type, callback) {
				$.get({
					url: baseApiUrl + this.endPoint + "/parentsForType/" + type,
					headers: headers
				}).done(callback);
			}

			this.getChildren = function(parentId, callback) {
				$.get({
					url: baseApiUrl + this.endPoint + "/childrenOf/" + parentId,
					headers: headers
				}).done(callback);
			}

		}
	}
}