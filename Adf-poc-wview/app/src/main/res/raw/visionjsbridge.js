var widgetMap = {};
var autoLoadWidgets = [];

var devicePixelRatio = 1;//window.devicePixelRatio;

function _getClassText(className){
    var x, sheets,classes;
    for( sheets=document.styleSheets.length-1; sheets>=0; sheets-- ){
        classes = document.styleSheets[sheets].rules || document.styleSheets[sheets].cssRules;
        for(x=0;x<classes.length;x++) {
            if(classes[x].selectorText.toLowerCase()===className.toLowerCase()){
                classStyleTxt = (classes[x].cssText ? classes[x].cssText : classes[x].style.cssText).match(/\{\s*([^{}]+)\s*\}/)[1];
                return classStyleTxt;
            }
        }
    }
    return false;
};

function _getTopPos(el) {
    for (var topPos = 0;
         el != null;
         topPos += el.offsetTop, el = el.offsetParent);
    return topPos;
}

function _getLeftPos(el) {
    for (var leftPos = 0;
         el != null;
         leftPos += el.offsetLeft, el = el.offsetParent);
    return leftPos;
}

/**
 *  Get frame of DOM element.
 *  Returns an object with x,y,width,height as properties.
 */
function _getFrame(el) {
    var _x=0,_y=0,_width=0,_height=0;
    if(el != null || el != undefined){
        _width = el.offsetWidth;
        _height = el.offsetHeight;
        for (;el != null;el = el.offsetParent){
            _x += el.offsetLeft;
            _y += el.offsetTop;
        }
    }
    return {
        x:_x*devicePixelRatio,
        y:_y*devicePixelRatio,
        width:_width*devicePixelRatio,
        height:_height*devicePixelRatio
    };
}


/**
 *  Retrieve the frames from the auto loaded widgets.
 */
function retrieveFramesFromLoadedWidgets() {
    var frameArray = [];
    var currentElem;
    var frameIdObject;
    autoLoadWidgets.forEach(function(currentId) {
       currentElem = document.getElementById(currentId);
       frameIdObject = {id:currentId, frame: _getFrame(currentElem)};
       //If width or height is 0 then the new frame is invalid
       if(frameIdObject.frame.width == 0 || frameIdObject.frame.height == 0){
           alert('Invalid frame for component ' + currentId + ': ' + JSON.stringify(frameIdObject.frame)) ;
       }
       frameArray.push(frameIdObject);
    });
    return frameArray;
}
       
/**
 *  Load the widgets which are mapped with a elements in the DOM.
 */
function loadWidgets() {
    for(i in autoLoadWidgets){
        var id = autoLoadWidgets[i];
        loadWidgetById(id);
    }
}

function sendMessage(command, operation, params, onSuccess, onFailure) {
    var jsonMessage = {
        "command":command,
        "operation":operation,
        "params": params,
        "onSuccess":onSuccess,
        "onFailure":onFailure
    };

    androidFromJS._bridgeCall(JSON.stringify(jsonMessage));
}

function postNotification(id, data) {
    var params = {
        "notificationId": id,
        "data": data
    };
    
    sendMessage("notification", "postNotification", params, "", "");
}


function removeAllComponents(){
    for(i in autoLoadWidgets){
        var id = autoLoadWidgets[i];
        removeComponent(widgetMap[id].id);
    }
}

function setAutoLoadWidgets (autoloadArray) {
    autoLoadWidgets = autoloadArray;
}

function excecuteAction(id, data, params, type) {
    var messageParams =  {
                "event": "select",
                "type" : type,
                "params": params,
                "data": data,
                "senderId": id
            };
    
    sendMessage("action", "excecuteAction", messageParams,"", "");
}

/**
 *  Generate new rotation action
 */
function rotationChangeAction() {
    var frameArray = retrieveFramesFromLoadedWidgets();
    if(frameArray.length > 0) {
        sendMessage("action", "rotate", frameArray,"", "");
    }
}

function orientationChange() {
    window.setTimeout(rotationChangeAction(), 2000);
}

function doTransition(url, transitionId, duration, wait){
    var messageParams = {
        "url":url,
        "transitionId":transitionId,
        "duration":duration,
        "wait":wait,
        "componentsToRemove":autoLoadWidgets
    };
    sendMessage("action", "transition", messageParams,"", "");
}

window.addEventListener("resize", orientationChange, false);




function replaceChildWidgets(widgetJson){
    if(widgetJson.components) {
        var i = 0;
        var newComponents = [];
        for(i = 0; i < widgetJson.components.length; i++){
            var component = widgetJson.components[i];
            if(component.path && (component.path.indexOf("id::") == 0)) {
                    //Replace component for widgetMap[id]
                    var widget = replaceChildWidgets(widgetMap[component.path.substring(4,component.path.length)]);
                    newComponents.push(widget);
            } else {
                newComponents.push(component);
            }
        }
        widgetJson.components = newComponents;
    }
    
    return widgetJson;
}

/**
 *  Load a widget which it is not mapped with a element in the DOM
 *  @param id: component id
 */
function showWidget(id){
    
    var widgetJSON = replaceChildWidgets(widgetMap[id]);
    
    var params = {
        "widgetName": "",
        "subWidgetName": "",
        "widgetJSON": widgetJSON,
        "cssText":""
    };
    
    
    sendMessage("widget", "createWidget", params, "", "");
}

function loadWidgetById(id){
    var widgetJSON = widgetMap[id];

    var widgetElement = document.getElementById(id);
    var widgetName = widgetElement.getAttribute('data-widget-name');
    if(widgetName == undefined) { widgetName= "";}
    var subWidgetName = widgetElement.getAttribute('data-sub-widget-name');
    if(subWidgetName == undefined) { subWidgetName= "";}

    var widgetJsonString = "";
    if(widgetElement){
        // overwrite these properties with the frame.
        var frame = _getFrame(widgetElement);
        widgetJSON.style.x = frame.x;
        widgetJSON.style.y = frame.y;
        widgetJSON.style.width = frame.width;
        widgetJSON.style.height = frame.height;

        console.log(JSON.stringify(widgetJSON.style));

        //If width or height is 0 then the frame is invalid
        if(frame.width == 0 || frame.height == 0){
            alert('Invalid frame for component ' + id + ': ' + JSON.stringify(frame)) ;
        }
    }
    widgetJSON.loadedFromWeb = "YES";
    
    //Workaround for widgets with empty data or properties coming from database
    if(widgetJSON.data == null){
        widgetJSON.data = {};
    }
    if(widgetJSON.properties == null){
        widgetJSON.properties = {};
    }
    
    var cssText = "";//_getClassText("."+widgetElement.className);
    widgetJSON = replaceChildWidgets(widgetJSON);
    
    var params ={
        "widgetName": widgetName,
        "subWidgetName": subWidgetName,
        "widgetJSON": widgetJSON,
        "cssText":cssText
    };
    
    sendMessage("widget", "createWidget", params, "", "");
}

function removeComponent(id){
    var params ={
        "componentId": id
    };
    
    sendMessage("widget", "removeWidget", params, "", "");
}

function excecuteAction(id, data, params, type) {
    var messageParams =  {
                "event": "select",
                "type" : type,
                "params": params,
                "data": data,
                "senderId": id
            };
    
    sendMessage("action", "excecuteAction", messageParams,"", "");
}

/**
 *  Generate new rotation action
 */
function rotationChangeAction() {
    var frameArray = retrieveFramesFromLoadedWidgets();
    if(frameArray.length > 0) {
        sendMessage("action", "rotate", frameArray,"", "");
    }
}

function orientationChange() {
    window.setTimeout(rotationChangeAction(), 2000);
}

function doTransition(url, transitionId, duration, wait){
    var messageParams = {
        "url":url,
        "transitionId":transitionId,
        "duration":duration,
        "wait":wait,
        "componentsToRemove":autoLoadWidgets
    };
    sendMessage("action", "transition", messageParams,"", "");
}

window.addEventListener("resize", orientationChange, false);

window.addEventListener("load",function(){
                        loadWidgets();
                        });

function onSelectBackButton() {
    removeAllComponents();
    window.location = "index.html";
}