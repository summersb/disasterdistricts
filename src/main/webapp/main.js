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
var gm = google.maps;
var oms;
var allAddresses;
var usualColor = 'eebb22';
var spiderfiedColor = 'ffee22';
var iconWithColor = function(color, number) {
    return 'http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=' + number + '|' + color + '|000000';
}
var shadow = new gm.MarkerImage(
    'https://www.google.com/intl/en_ALL/mapfiles/shadow50.png',
    new gm.Size(37, 34),  // size   - for sprite clipping
    new gm.Point(0, 0),   // origin - ditto
    new gm.Point(10, 34)  // anchor - where to meet map location
    );

function initialize() {
    $.getJSON('rest/list', function(data){
        allAddresses = data;
        plot();
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
        var ad = getAddressFromList(marker.desc, allAddresses);
        var text = ad.primary?'Remove Leader':'Make Leader';
        iw.setContent(marker.desc + " at " + ad.address + "<br><a onclick=\"javascript: makeLeader('" + ad.household + "');\">" + text + "</a>");
        iw.open(map, marker);
    });
    oms.addListener('spiderfy', function(markers) {
        for(var i = 0; i < markers.length; i ++) {
            markers[i].setIcon(iconWithColor(spiderfiedColor));
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
    allAddresses.forEach(getAddress);
}
function clearMarker(element){
    element.setMap(null);
}

function getAddress(address, index, array) {
    //    console.log(address.household);
    if(address.primary){
        markLeader(new google.maps.LatLng(address.lat,address.lng));
    }
    markHouse(address);
}

function markHouse(address){
    latlng = new gm.LatLng(address.lat,address.lng);
    // if marker has latlng change icon color
    var color = usualColor;
    allAddresses.forEach(function(element){
        ll = new gm.LatLng(element.lat, element.lng);
        if(latlng.equals(ll) && element.household != address.household){
            color = 'cccccc';
        //            console.log("Same address "+ address.household + " " + latlng.toString() + " as " + element.household + " " + ll.toString());
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

function makeLeader(name){
    var address = getAddressFromList(name, allAddresses);
    if(address.primary){
        address.primary = false;
    }else{
        address.primary = true;
    }
    $.ajax({
        type: 'POST',
        url: "rest/list",
        data: JSON.stringify(address),
        contentType: 'application/json;charset=UTF-8',
        success: function(data, textStatus, xhr){
            alert(data);
        }
    });
    plotHouses(radius);
}

function markLeader(latlng){
    var populationOptions = {
        strokeColor: '#FF0000',
        strokeOpacity: 0.8,
        strokeWeight: 2,
        fillColor: '#FF0000',
        fillOpacity: 0.35,
        map: map,
        center: latlng,
        radius: radius
    };
    cityCircle = new gm.Circle(populationOptions);
    circles.push(cityCircle);
    allAddresses.forEach(test.bind(null, cityCircle) );
    //    console.log("Leader:" + leader.household);
    for(i=0;i<pe.length;i++){
        //        console.log(pe[i].household);
        }
    pe = [];
}
var pe = [];

function test(circle, element, index, array){
    var ll = new gm.LatLng(element.lat,element.lng);
    if(contains(circle, ll)){
        pe.push(element);
    }
}

function contains(circle, latLng) {
    var insideBounds = circle.getBounds().contains(latLng);
    var dist;
    if(gm.geometry.spherical.computeDistanceBetween(circle.getCenter(), latLng) <= circle.getRadius()){
        dist = true;
    }else{
        dist = false;
    }
    return insideBounds && dist;
}

function getAddressFromList(name, list){
    for(var i=0;i<list.length; i++){
        if(list[i].household == name){
            return list[i];
        }
    }
    return null;
}

function createDistricts(){
    // extract leaders
    var leaders = [];
    for(var i=0; i<allAddresses.length; i++){
        if(allAddresses[i].primary){
            allAddresses[i].district = i;
            leaders.push(new gm.LatLng(allAddresses[i].lat, allAddresses[i].lng));
        }
    }
    // for each house compute closest leader

    for(i=0; i<allAddresses.length; i++){
        var distance = [];
        for(var j=0; j<leaders.length; j++){
            // compute dist
            if(allAddresses[i].lat){
                var ll = new gm.LatLng(allAddresses[i].lat, allAddresses[i].lng);
                var dis = gm.geometry.spherical.computeDistanceBetween(ll, leaders[j]);
                distance.push(dis);
            }
        }
        var pos = 0;
        for(j=1; j<distance.length; j++){
            if(distance[j]<distance[pos]){
                pos = j;
            }
        }
        allAddresses[i].district = pos;
    }
    for(i=0; i<allAddresses.length; i++){
        $.ajax({
            type: 'POST',
            url: "rest/list",
            data: JSON.stringify(allAddresses[i]),
            contentType: 'application/json;charset=UTF-8',
            success: function(data, textStatus, xhr){
                alert(data);
            }
        });
    }
}