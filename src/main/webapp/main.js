/*
 * Copyright (C) 2012
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
var radius = 500;
var markers = [];
var circles = [];
var districts = [];
var gm = google.maps;
var oms;
var map;
var allAddresses;
var usualColor = 'eebb22';
var spiderfiedColor = 'ffee22';
var iconWithColor = function(color, number) {
    return 'http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=' + number + '|' + color + '|000000';
};
var shadow = new gm.MarkerImage(
    'https://www.google.com/intl/en_ALL/mapfiles/shadow50.png',
    new gm.Size(37, 34),  // size   - for sprite clipping
    new gm.Point(0, 0),   // origin - ditto
    new gm.Point(10, 34)  // anchor - where to meet map location
    );

function initialize() {
    $.getJSON('rest/member/list', function(data){
        allAddresses = data;
        plot();
        $.getJSON('rest/district/list', function(data){
            districts = data;
            showLeader();
            showAssistant();
        });
    });
}

function plot(){
    var mapOptions = {
        center: new gm.LatLng(33.200037,-117.242536),
        zoom: 14,
        mapTypeId: gm.MapTypeId.ROADMAP
    };
    map = new gm.Map(document.getElementById("map_canvas"), mapOptions);
    oms = new OverlappingMarkerSpiderfier(map, {
        markersWontMove: true,
        markersWontHide: true
    });

    var iw = new gm.InfoWindow();
    oms.addListener('click', function(marker) {
        // find address
        var ad = marker.address;
        var content = marker.desc + " at " + ad.address + "<br>";
        var district = getDistrict(ad.district);
        if(district){
            content += "District Leader is " + (district.leader?district.leader.household:"None") + "<br>";
            content += "District Assistant is " + (district.assistant?district.assistant.household:"None") + "<br>";
        }

        if(district){
            // assign as leader
            content += "<a onclick=\"javascript: assignLeader('"
            + marker.address.household
            + "', " + district.id + ")\">Assign as Leader</a><br>";
            // assign as assistant
            content += "<a onclick=\"javascript: assignAssistant('"
            + marker.address.household
            + "', " + district.id + ")\">Assign as Assistant</a><br>";
        }
        // add link to create a new district
        content += "<a onclick=\"javascript: createNewDistrict('" + 
                marker.address.household + "')\">Create New District</a><br>";
        
        // change district
        content += "<a onclick=\"javascript: assignDistrict('"
        + marker.address.household
        + "', document.getElementById('districtselect').selectedIndex)\">Change to District</a>";
        content += "<select id='districtselect'>";
        for(var i=0;i<districts.length;i++){
            var selected = "";
            if(marker.address.district === districts[i].id){
                selected = " selected";
            }
            content += "<option value='" + districts[i].id + "'" + selected + ">("
            + districts[i].id + ") " + districts[i].leader.household + "</option>";
        }
        content += "</select>";
        iw.setContent(content);
        iw.open(map, marker);
    });
    oms.addListener('spiderfy', function(markers) {
        for(var i = 0; i < markers.length; i ++) {
            markers[i].setIcon(iconWithColor(spiderfiedColor, markers[i].address.district));
            markers[i].setShadow(null);
        }
        iw.close();
    });
    oms.addListener('unspiderfy', function(markers) {
        for(var i = 0; i < markers.length; i ++) {
            markers[i].setIcon(iconWithColor('cccccc', markers[i].address.district));
            markers[i].setShadow(shadow);
        }
    });
    // plot my address
    plotHouses('500');
//    var markerCluster = new MarkerClusterer(map, markers);
}

function plotHouses(r){
    markers.forEach(clearMarker);
    circles.forEach(clearMarker);
    circles = [];
    radius = parseInt(r);
    allAddresses.forEach(markHouse);
}
function clearMarker(element){
    element.setMap(null);
}

function markHouse(address){
    latlng = new gm.LatLng(address.lat,address.lng);
    // if marker has latlng change icon color
    var color = usualColor;
    allAddresses.forEach(function(element){
        ll = new gm.LatLng(element.lat, element.lng);
        if(latlng.equals(ll) && element.household !== address.household){
            color = 'cccccc';
        }
    });
    var mkr = new gm.Marker({
        position:latlng,
        map: map,
        title: address.household,
        icon: iconWithColor(color,address.district),
        shadow: shadow
    });
    mkr.desc = address.household;
    mkr.address = address;
    mkr.setVisible(true);
    markers.push(mkr);
    oms.addMarker(mkr);
}

function assignLeader(name, id){
    var address = getAddressFromList(name, allAddresses);
    var district = getDistrict(id);
    district.leader = address;
    postDistrict(district);
}

function assignAssistant(name, id){
    var address = getAddressFromList(name, allAddresses);
    var district = getDistrict(id);
    district.assistant = address;
    postDistrict(district);
}

function postDistrict(district){
    $.ajax({
        type: 'POST',
        url: "rest/district",
        data: JSON.stringify(district),
        contentType: 'application/json;charset=UTF-8',
        success: function(data, textStatus, xhr){
        }
    });
    plotHouses(radius);
    showLeader();
    showAssistant();
}

function createNewDistrict(leader){
    $.ajax({
        type: 'POST',
        url: "rest/district/create/" + leader,
        success: function(data, textStatus, xhr){
            districts.push(data);
            var address = getAddressFromList(leader, allAddresses);
            address.district = data.id;
            plotHouses(radius);
            showLeader();
        }
    });
}

function assignDistrict(name, id){
    var address = getAddressFromList(name, allAddresses);
    var district = getDistrict(address.district);
    address.district = id;
    $.ajax({
        type: 'PUT',
        url: "rest/member",
        data: JSON.stringify(address),
        contentType: 'application/json;charset=UTF-8',
        success: function(data, textStatus, xhr){
        }
    });
    if(district !== null){
        // undo if leader or assistant
        if(district.leader !== null && district.leader.household === address.household){
            district.leader = null;
            postDistrict(district);
        }else if(district.assistant !== null && district.assistant.household === address.household){
            district.assistant = null;
            postDistrict(district);
        }
    }
    plotHouses(radius);
    showLeader();
    showAssistant();
}

function showLeader(){
    for(var i=0;i<districts.length;i++){
        var latlng = new gm.LatLng(districts[i].leader.lat, districts[i].leader.lng);
        var circleOptions = {
            strokeColor: '#FF0000',
            strokeOpacity: 0.8,
            strokeWeight: 2,
            fillColor: '#FF0000',
            fillOpacity: 0.35,
            map: map,
            center: latlng,
            radius: radius
        };
        circle = new gm.Circle(circleOptions);
        circles.push(circle);
    }
}

function showAssistant(){
    for(var i=0;i<districts.length;i++){
        if(districts[i].assistant === null){
            continue;
        }
        var latlng = new gm.LatLng(districts[i].assistant.lat, districts[i].assistant.lng);
        var circleOptions = {
            strokeColor: '#00FF00',
            strokeOpacity: 0.8,
            strokeWeight: 2,
            fillColor: '#00FF00',
            fillOpacity: 0.35,
            map: map,
            center: latlng,
            radius: radius
        };
        circle = new gm.Circle(circleOptions);
        circles.push(circle);
    }
}

function getAddressFromList(name, list){
    for(var i=0;i<list.length; i++){
        if(list[i].household === name){
            return list[i];
        }
    }
    return null;
}

function getDistrict(id){
    for(var i=0;i<districts.length; i++){
        if(districts[i].id === id){
            return districts[i];
        }
    }
    return null;
}