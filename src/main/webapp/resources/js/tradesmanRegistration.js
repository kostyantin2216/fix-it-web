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


function ThymeleafScript(baseApiUrl, authorizationToken, initialMapAreaType, areas) {

	// Script Global Variable
	// ======================================

	var mapWrapper = null;
	var mapControl = null;
	var mapSelections = null;
	var apiAccess = new ApiAccess(baseApiUrl, authorizationToken);


	// Functions
	// ======================================

	this.initMap = function() {
		mapSelections = new MapSelections();

		var johannesburg = { lat: -26.171, lng: 28.110 };
		mapWrapper = new MapWrapper(document.getElementById('map'),  johannesburg);
		mapWrapper.registerSelectionListener(mapSelections);

		mapControl = new MapControl(mapWrapper, johannesburg);

		apiAccess.getMapAreas(initialMapAreaType, mapWrapper.showAreas);
	}
	
	this.prepareForm = function() {
		$("#workingAreas").val(mapSelections.toInputValue());
		
		var telephoneInput = $("#telephone");
		//if(telephoneInput.intlTelInput("isValidNumber")) {
			telephoneInput.val(telephoneInput.intlTelInput("getNumber"));
		/*} else {
			
		}*/
	}


	// Classes
	// =======================================

	function MapSelections() {
		var selectedAreas = [];

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




	function MapControl(mapWrapper, center) {
		var controlDiv = document.createElement('div');
		controlDiv.id = 'map-control'; 

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