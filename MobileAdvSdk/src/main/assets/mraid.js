var mraid = window.mraid = {};

var bridge = window.mraidbridge = {
    lastSizeChangeProperties: null,
};

bridge.fireChangeEvent = function(properties) {
    for (var p in properties) {
        if (properties.hasOwnProperty(p)) {
            // Change handlers defined by MRAID below
            var handler = changeHandlers[p];
            handler(properties[p]);
        }
    }
};

bridge.setCurrentPosition = function(x, y, width, height) {
    currentPosition = {
        x: x,
        y: y,
        width: width,
        height: height
    };
};

bridge.setDefaultPosition = function(x, y, width, height) {
    defaultPosition = {
        x: x,
        y: y,
        width: width,
        height: height
    };
};

bridge.setMaxSize = function(width, height) {
    maxSize = {
        width: width,
        height: height
    };

    expandProperties.width = width;
    expandProperties.height = height;
};

bridge.setPlacementType = function(_placementType) {
    placementType = _placementType;
};

bridge.setScreenSize = function(width, height) {
    screenSize = {
        width: width,
        height: height
    };
};

bridge.setState = function(_state) {
    state = _state;
    broadcastEvent(EVENTS.STATECHANGE, state);
};

bridge.setIsViewable = function(_isViewable) {
    isViewable = _isViewable;
    broadcastEvent(EVENTS.VIEWABLECHANGE, isViewable);
};

bridge.setSupports = function(sms, tel, calendar, storePicture, inlineVideo, sdk) {
    supportProperties = {
        sms: sms,
        tel: tel,
        calendar: calendar,
        storePicture: storePicture,
        inlineVideo: inlineVideo,
        sdk: sdk
    };
};

bridge.notifyReadyEvent = function() {
    broadcastEvent(EVENTS.READY);
};

bridge.notifyErrorEvent = function(message, action) {
    broadcastEvent(EVENTS.ERROR, message, action);
};

bridge.notifySizeChangeEvent = function(width, height) {
    if (this.lastSizeChangeProperties &&
        width === this.lastSizeChangeProperties.width && height === this.lastSizeChangeProperties.height) {
        return;
    }

    this.lastSizeChangeProperties = {
        width: width,
        height: height
    };
    broadcastEvent(EVENTS.SIZECHANGE, width, height);
};

bridge.notifyStateChangeEvent = function() {
    if (state === STATES.LOADING) {
    }

    broadcastEvent(EVENTS.STATECHANGE, state);
};

bridge.notifyViewableChangeEvent = function() {
    broadcastEvent(EVENTS.VIEWABLECHANGE, isViewable);
};

bridge.sendMessageToSDK = function (value) {
    try {
        MraidController.sendMessageToSDK(JSON.stringify(value));
    }
    catch (e) {
    }
    console.log('send message to sdk '+ JSON.stringify(value));
}

var VERSION = mraid.VERSION = '3.0';

var STATES = mraid.STATES = {
    LOADING: 'loading',
    DEFAULT: 'default',
    EXPANDED: 'expanded',
    HIDDEN: 'hidden',
    RESIZED: 'resized'
};

var EVENTS = mraid.EVENTS = {
    ERROR: 'error',
    READY: 'ready',
    SIZECHANGE: 'sizeChange',
    STATECHANGE: 'stateChange',
    EXPOSURECHANGE: 'exposureChange',
    AUDIOVOLUMECHANGE: 'audioVolumeChange',
    VIEWABLECHANGE: 'viewableChange',
    INFO: 'info'
};

var PLACEMENT_TYPES = mraid.PLACEMENT_TYPES = {
    UNKNOWN: 'unknown',
    INLINE: 'inline',
    INTERSTITIAL: 'interstitial'
};

// Functions that will be invoked by the native SDK whenever a "change" event occurs.
var changeHandlers = {
    state: function(val) {
        if (state === STATES.LOADING) {
            broadcastEvent(EVENTS.INFO, 'Native SDK initialized.');
        }
        state = val;
        broadcastEvent(EVENTS.INFO, 'Set state to ' + JSON.stringify(val));
        broadcastEvent(EVENTS.STATECHANGE, state);
    },

    viewable: function(val) {
        isViewable = val;
        broadcastEvent(EVENTS.INFO, 'Set isViewable to ' + JSON.stringify(val));
        broadcastEvent(EVENTS.VIEWABLECHANGE, isViewable);
    },

    placementType: function(val) {
        placementType = val;
        broadcastEvent(EVENTS.INFO, 'Set placementType to ' + JSON.stringify(val));
    },

    sizeChange: function(val) {
        for (var key in val) {
            if (val.hasOwnProperty(key)) screenSize[key] = val[key];
        }
        broadcastEvent(EVENTS.INFO, 'Set screenSize to ' + JSON.stringify(val));
    },

    supports: function(val) {
        supportProperties = val;
        broadcastEvent(EVENTS.INFO, 'Set supports to ' + JSON.stringify(val));
    },

    currentAppOrientation: function (val) {
        currentAppOrientation = val;
        broadcastEvent(EVENTS.INFO, 'Set currentAppOrientation to' + JSON.stringify(val));
    },

    currentPosition: function (val) {
        currentPosition = val;
        broadcastEvent(EVENTS.INFO, 'Set currentPosition to ' + JSON.stringify(val));
    },

    defaultPosition: function (val) {
        defaultPosition = val;
        broadcastEvent(EVENTS.INFO, 'Set defaultPosition to ' + JSON.stringify(val));
    },

    expandProperties: function (val) {
        expandProperties = val;
        broadcastEvent(EVENTS.INFO, 'Set expandProperties to ' + JSON.stringify(val));
    },

    maxSize: function (val) {
        maxSize = val;
        broadcastEvent(EVENTS.INFO, 'Set maxSize to ' + JSON.stringify(val));
    },

    screenSize: function (val) {
        screenSize = val;
        broadcastEvent(EVENTS.INFO, 'Set screenSize to ' + JSON.stringify(val));
    },

    geoLocation: function (val) {
        geoLocation = val;
        broadcastEvent(EVENTS.INFO, 'Set geoLocation to ' + JSON.stringify(val));
    },
    
    rewarded: function (val) {
        isRewarded = val;
        broadcastEvent(EVENTS.INFO, 'Set isRewarded to ' + JSON.stringify(val))
    }
};

var contains = function (value, array) {
    for (var i in array) {
        if (array[i] === value) return true;
    }
    return false;
};

var validate = function (obj, validators, action, merge) {
    if (!merge) {
        // Check to see if any required properties are missing.
        if (obj === null) {
            broadcastEvent(EVENTS.ERROR, 'Required object not provided.', action);
            return false;
        } else {
            for (var i in validators) {
                if (validators.hasOwnProperty(i) && obj[i] === undefined) {
                    broadcastEvent(EVENTS.ERROR, 'Object is missing required property: ' + i, action);
                    return false;
                }
            }
        }
    }

    for (var prop in obj) {
        var validator = validators[prop];
        var value = obj[prop];
        if (validator && !validator(value)) {
            // Failed validation.
            broadcastEvent(EVENTS.ERROR, 'Value of property ' + prop + ' is invalid: ' + value, action);
            return false;
        }
    }
    return true;
};

var supportProperties = {
    sms: false,
    tel: false,
    calendar: false,
    storePicture: false,
    inlineVideo: false,
    sdk: false
};

// Properties which define the behavior of an expandable ad.
var expandProperties = {
    width: 0,
    height: 0,
    useCustomClose: false,
    isModal: true
};

var resizeProperties = {
    width: false,
    height: false,
    offsetX: false,
    offsetY: false,
    customClosePosition: 'top-right',
    allowOffscreen: true
};

var orientationProperties = {
    allowOrientationChange: true,
    forceOrientation: "none"
};

var currentAppOrientation = {
    orientation: "portrait",
    locked: false
};

var placementType = PLACEMENT_TYPES.UNKNOWN;

var listeners = {};

var currentPosition = {};

var defaultPosition = {};

var maxSize = {};

var screenSize = {};

var geoLocation = {};

var state = STATES.LOADING;

var isViewable = false;

var isRewarded = false;

var expandPropertyValidators = {
    useCustomClose: function (v) {
        return (typeof v === 'boolean');
    },
};

var resizePropertyValidators = {
    width: function (v) {
        return !isNaN(v) && v > 0;
    },
    height: function (v) {
        return !isNaN(v) && v > 0;
    },
    offsetX: function (v) {
        return !isNaN(v);
    },
    offsetY: function (v) {
        return !isNaN(v);
    },
    customClosePosition: function (v) {
        return (typeof v === 'string' &&
            ['top-right', 'bottom-right', 'top-left', 'bottom-left', 'center', 'top-center', 'bottom-center'].indexOf(v) > -1);
    },
    allowOffscreen: function (v) {
        return (typeof v === 'boolean');
    }
};

var CalendarEventParser = {
    initialize: function (parameters) {
        this.parameters = parameters;
        this.errors = [];
        this.arguments = ['createCalendarEvent'];
    },

    parse: function () {
        if (!this.parameters) {
            this.errors.push('The object passed to createCalendarEvent cannot be null.');
        } else {
            this.parseDescription();
            this.parseLocation();
            this.parseSummary();
            this.parseStartAndEndDates();
            this.parseReminder();
            this.parseRecurrence();
            this.parseTransparency();
        }

        var errorCount = this.errors.length;
        if (errorCount) {
            this.arguments.length = 0;
        }

        return (errorCount === 0);
    },

    parseDescription: function () {
        this._processStringValue('description');
    },

    parseLocation: function () {
        this._processStringValue('location');
    },

    parseSummary: function () {
        this._processStringValue('summary');
    },

    parseStartAndEndDates: function () {
        this._processDateValue('start');
        this._processDateValue('end');
    },

    parseReminder: function () {
        var reminder = this._getParameter('reminder');
        if (!reminder) {
            return;
        }

        if (reminder < 0) {
            this.arguments.push('relativeReminder');
            this.arguments.push(parseInt(reminder) / 1000);
        } else {
            this.arguments.push('absoluteReminder');
            this.arguments.push(reminder);
        }
    },

    parseRecurrence: function () {
        var recurrenceDict = this._getParameter('recurrence');
        if (!recurrenceDict) {
            return;
        }

        this.parseRecurrenceInterval(recurrenceDict);
        this.parseRecurrenceFrequency(recurrenceDict);
        this.parseRecurrenceEndDate(recurrenceDict);
        this.parseRecurrenceArrayValue(recurrenceDict, 'daysInWeek');
        this.parseRecurrenceArrayValue(recurrenceDict, 'daysInMonth');
        this.parseRecurrenceArrayValue(recurrenceDict, 'daysInYear');
        this.parseRecurrenceArrayValue(recurrenceDict, 'monthsInYear');
    },

    parseTransparency: function () {
        var validValues = ['opaque', 'transparent'];

        if (this.parameters.hasOwnProperty('transparency')) {
            var transparency = this.parameters.transparency;
            if (contains(transparency, validValues)) {
                this.arguments.push('transparency');
                this.arguments.push(transparency);
            } else {
                this.errors.push('transparency must be opaque or transparent');
            }
        }
    },

    parseRecurrenceArrayValue: function (recurrenceDict, kind) {
        if (recurrenceDict.hasOwnProperty(kind)) {
            var array = recurrenceDict[kind];
            if (!array || !(array instanceof Array)) {
                this.errors.push(kind + ' must be an array.');
            } else {
                var arrayStr = array.join(',');
                this.arguments.push(kind);
                this.arguments.push(arrayStr);
            }
        }
    },

    parseRecurrenceInterval: function (recurrenceDict) {
        if (recurrenceDict.hasOwnProperty('interval')) {
            var interval = recurrenceDict.interval;
            if (!interval) {
                this.errors.push('Recurrence interval cannot be null.');
            } else {
                this.arguments.push('interval');
                this.arguments.push(interval);
            }
        } else {
            // If a recurrence rule was specified without an interval, use a default value of 1.
            this.arguments.push('interval');
            this.arguments.push(1);
        }
    },

    parseRecurrenceFrequency: function (recurrenceDict) {
        if (recurrenceDict.hasOwnProperty('frequency')) {
            var frequency = recurrenceDict.frequency;
            var validFrequencies = ['daily', 'weekly', 'monthly', 'yearly'];
            if (contains(frequency, validFrequencies)) {
                this.arguments.push('frequency');
                this.arguments.push(frequency);
            } else {
                this.errors.push('Recurrence frequency must be one of: "daily", "weekly", "monthly", "yearly".');
            }
        }
    },

    parseRecurrenceEndDate: function (recurrenceDict) {
        var expires = recurrenceDict.expires;

        if (!expires) {
            return;
        }

        this.arguments.push('expires');
        this.arguments.push(expires);
    },

    _getParameter: function (key) {
        if (this.parameters.hasOwnProperty(key)) {
            return this.parameters[key];
        }

        return null;
    },

    _processStringValue: function (kind) {
        if (this.parameters.hasOwnProperty(kind)) {
            var value = this.parameters[kind];
            this.arguments.push(kind);
            this.arguments.push(value);
        }
    },

    _processDateValue: function (kind) {
        if (this.parameters.hasOwnProperty(kind)) {
            var dateString = this._getParameter(kind);
            this.arguments.push(kind);
            this.arguments.push(dateString);
        }
    }
};

var EventListeners = function (event) {
    this.event = event;
    this.count = 0;
    var listeners = {};

    this.add = function (func) {
        var id = String(func);
        if (!listeners[id]) {
            listeners[id] = func;
            this.count++;
        }
    };

    this.remove = function (func) {
        var id = String(func);
        if (listeners[id]) {
            listeners[id] = null;
            delete listeners[id];
            this.count--;
            return true;
        } else {
            return false;
        }
    };

    this.removeAll = function () {
        for (var id in listeners) {
            if (listeners.hasOwnProperty(id)) this.remove(listeners[id]);
        }
    };

    this.broadcast = function (args) {
        for (var id in listeners) {
            if (listeners.hasOwnProperty(id)) {
                listeners[id].apply(mraid, args);
            }
        }
    };

    this.toString = function () {
        var out = [event, ':'];
        for (var id in listeners) {
            if (listeners.hasOwnProperty(id)) out.push('|', id, '|');
        }
        return out.join('');
    };
};

var broadcastEvent = function () {
    var args = new Array(arguments.length);
    var l = arguments.length;
    for (var i = 0; i < l; i++) args[i] = arguments[i];
    var event = args.shift();
    if (listeners[event]) listeners[event].broadcast(args);
//    MraidController.broadcastEvent(event, args)
};

// FUNCTIONS

mraid.isViewable = function () {
    return isViewable;
};

mraid.setRewarded = function (rewarded) {
    isRewarded = rewarded;
}

mraid.getRewarded = function () {
    return isRewarded;
}

mraid.getVersion = function () {
    return mraid.VERSION;
};

mraid.addEventListener = function (event, listener) {
    if (!event || !listener) {
        broadcastEvent(EVENTS.ERROR, 'Both event and listener are required.', 'addEventListener');
    } else if (!contains(event, EVENTS)) {
        broadcastEvent(EVENTS.ERROR, 'Unknown MRAID event: ' + event, 'addEventListener');
    } else {
        if (!listeners[event]) {
            listeners[event] = new EventListeners(event);
        }
        listeners[event].add(listener);
    }
};

mraid.removeEventListener = function (event, listener) {
    if (!event) {
        broadcastEvent(EVENTS.ERROR, 'Event is required.', 'removeEventListener');
        return;
    }

    if (listener) {
        // If we have a valid event, we'll try to remove the listener from it.
        var success = false;
        if (listeners[event]) {
            success = listeners[event].remove(listener);
        }

        // If we didn't have a valid event or couldn't remove the listener from the event, broadcast an error and return early.
        if (!success) {
            broadcastEvent(EVENTS.ERROR, 'Listener not currently registered for event.', 'removeEventListener');
            return;
        }

    } else if (!listener && listeners[event]) {
        listeners[event].removeAll();
    }

    if (listeners[event] && listeners[event].count === 0) {
        listeners[event] = null;
        delete listeners[event];
    }
};

mraid.open = function (URL) {
    if (!URL) {
        broadcastEvent(EVENTS.ERROR, 'URL is required.', 'open');
    }
    else
    {
        var obj = {
            m_eventType: "open",
            m_uri: URL
        }

        bridge.sendMessageToSDK(obj);
    }
};

mraid.close = function () {
    if (state === STATES.HIDDEN) {
        broadcastEvent(EVENTS.ERROR, 'Ad cannot be closed when it is already hidden.',
            'close');
    } else {
        var obj = {
            m_eventType: "close"
        }

        bridge.sendMessageToSDK(obj);
    }
};

mraid.unload = function () {
    var obj = {
        m_eventType: "unload"
    }

    bridge.sendMessageToSDK(obj);
}

mraid.expand = function (URL) {
    if (!(this.getState() === STATES.DEFAULT || this.getState() === STATES.RESIZED)) {
        broadcastEvent(EVENTS.ERROR, 'Ad can only be expanded from the default or resized state.', 'expand');
    } else {
        var args = ['expand',
            'shouldUseCustomClose', expandProperties.useCustomClose
        ];

        if (URL) {
            args = args.concat(['url', URL]);
        }

        var obj = {
            m_eventType: "expand"
        }
        if (URL)
        {
            obj.m_url = URL;
        }

        bridge.sendMessageToSDK(obj);
    }
};

mraid.playVideo = function (uri) {
    // if (!mraid.isViewable()) {
    //     broadcastEvent(EVENTS.ERROR, 'playVideo cannot be called until the ad is viewable', 'playVideo');
    //     return;
    // }

    if (!uri) {
        broadcastEvent(EVENTS.ERROR, 'playVideo must be called with a valid URI', 'playVideo');
    } else {
        var obj = {
            m_eventType: "playVideo",
            m_uri: URL
        }

        bridge.sendMessageToSDK(obj);
    }
};

mraid.resize = function () {
    if (!(this.getState() === STATES.DEFAULT || this.getState() === STATES.RESIZED)) {
        broadcastEvent(EVENTS.ERROR, 'Ad can only be resized from the default or resized state.', 'resize');
    } else if (!resizeProperties.width || !resizeProperties.height) {
        broadcastEvent(EVENTS.ERROR, 'Must set resize properties before calling resize()', 'resize');
    } else {
        var obj = {
            m_eventType: "resize",
            m_width: resizeProperties.width,
            m_height: resizeProperties.height,
            m_offsetX: resizeProperties.offsetX || 0,
            m_offsetY: resizeProperties.offsetY || 0,
            m_customClosePosition: resizeProperties.customClosePosition,
            m_allowOffscreen: !!resizeProperties.allowOffscreen
        }

        bridge.sendMessageToSDK(obj);
    }
};

mraid.storePicture = function (uri) {
    // if (!mraid.isViewable()) {
    //     broadcastEvent(EVENTS.ERROR, 'storePicture cannot be called until the ad is viewable', 'storePicture');
    //     return;
    // }

    if (!uri) {
        broadcastEvent(EVENTS.ERROR, 'storePicture must be called with a valid URI', 'storePicture');
    } else {
        var obj = {
            m_eventType: "storePicture",
            m_uri: uri
        }

        bridge.sendMessageToSDK(obj);
    }
};

mraid.createCalendarEvent = function (parameters) {
    var obj = {
        m_eventType: "createCalendarEvent"
    }

    bridge.sendMessageToSDK(obj);
    // CalendarEventParser.initialize(parameters);
    // if (CalendarEventParser.parse()) {
    //     var obj = {
    //         m_eventType: "createCalendarEvent"
    //     }
    //
    //     Unity.call(JSON.stringify(obj));
    // } else {
    //     broadcastEvent(EVENTS.ERROR, CalendarEventParser.errors[0], 'createCalendarEvent');
    // }
};

// PROPERTIES

mraid.supports = function (feature) {
    return supportProperties[feature];
};

mraid.getPlacementType = function () {
    return placementType;
};

mraid.getOrientationProperties = function () {
    return {
        allowOrientationChange: orientationProperties.allowOrientationChange,
        forceOrientation: orientationProperties.forceOrientation
    };
};

mraid.setOrientationProperties = function (properties) {

    if (properties.hasOwnProperty('allowOrientationChange')) {
        orientationProperties.allowOrientationChange = properties.allowOrientationChange;
    }

    if (properties.hasOwnProperty('forceOrientation')) {
        orientationProperties.forceOrientation = properties.forceOrientation;
    }

    var obj = {
        m_eventType: "setOrientationProperties",
        m_allowOrientationChange: orientationProperties.allowOrientationChange,
        m_forceOrientation: orientationProperties.forceOrientation
    }

    bridge.sendMessageToSDK(obj);
};

mraid.getCurrentAppOrientation = function () {
    return {
        orientation: currentAppOrientation.orientation,
        locked: currentAppOrientation.locked
    }
}

mraid.getCurrentPosition = function () {
    return {
        x: currentPosition.x,
        y: currentPosition.y,
        width: currentPosition.width,
        height: currentPosition.height
    };
};

mraid.getDefaultPosition = function () {
    return {
        x: defaultPosition.x,
        y: defaultPosition.y,
        width: defaultPosition.width,
        height: defaultPosition.height
    };
};

mraid.getState = function () {
    return state;
};

mraid.getExpandProperties = function () {
    var properties = {
        width: expandProperties.width,
        height: expandProperties.height,
        useCustomClose: expandProperties.useCustomClose,
        isModal: expandProperties.isModal
    };
    return properties;
};

mraid.setExpandProperties = function (properties) {
    if (validate(properties, expandPropertyValidators, 'setExpandProperties', true)) {
        expandProperties.height = properties.height;
        expandProperties.width = properties.width;
        expandProperties.useCustomClose = properties.useCustomClose;
        expandProperties.isModal = properties.isModal;

        var obj = {
            m_eventType: "setExpandProperties",
            m_width: expandProperties.width,
            m_height: expandProperties.height,
            m_useCustomClose: expandProperties.useCustomClose,
            m_isModal: expandProperties.isModal
        }

        bridge.sendMessageToSDK(obj);
    }
};

mraid.rewardReceived = function (rewardReceived) {
    var obj = {
        m_eventType: "rewardReceived",  
        m_value: rewardReceived
    };
    
    bridge.sendMessageToSDK(obj);
}

mraid.getMaxSize = function () {
    return {
        width: maxSize.width,
        height: maxSize.height
    };
};

mraid.getScreenSize = function () {
    return {
        width: screenSize.width,
        height: screenSize.height
    };
};

mraid.getResizeProperties = function () {
    var properties = {
        width: resizeProperties.width ,
        height: resizeProperties.height,
        offsetX: resizeProperties.offsetX,
        offsetY: resizeProperties.offsetY,
        customClosePosition: resizeProperties.customClosePosition,
        allowOffscreen: resizeProperties.allowOffscreen
    };
    return properties;
};

mraid.setResizeProperties = function (properties) {
    if (validate(properties, resizePropertyValidators, 'setResizeProperties', true)) {

        resizeProperties.width = properties.width;
        resizeProperties.height = properties.height;
        resizeProperties.offsetX = properties.offsetX || 0;
        resizeProperties.offsetY = properties.offsetY || 0;
        resizeProperties.customClosePosition = properties.customClosePosition || "top-right";
        resizeProperties.allowOffscreen = properties.allowOffscreen || false;
    }
};

mraid.getLocation = function () {
    return {
        lat: geoLocation.lat || undefined,
        lon: geoLocation.lon || undefined,
        type: geoLocation.type || undefined,
        accuracy: geoLocation.accuracy || undefined,
        lastfix: geoLocation.lastfix || undefined,
        ipservice: geoLocation.ipservice || undefined
    };
}

mraid.sendContentReadyEvent = function (isLoaded) {
    var obj = {
        m_eventType: "contentLoaded",
        m_value: isLoaded
    };

    bridge.sendMessageToSDK(obj);
}

mraid.addEventListener(EVENTS.INFO, bridge.sendMessageToSDK);