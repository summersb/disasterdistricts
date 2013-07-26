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
            url: "rest/member",
            data: JSON.stringify(allAddresses[i]),
            contentType: 'application/json;charset=UTF-8',
            success: function(data, textStatus, xhr){
                alert(data);
            }
        });
    }
}

function assignToDistricts(){
    var distConfig = [];
    for(var i=0; i<districts.length; i++){
        var dist = districts[i];
        // get all members within 1km of leader
        var distMembers = getMembersInDistrict(dist);
        if(distMembers.length === 0){
            continue;
        }

        console.log("Members for district " + dist.id + " dist size: " + distMembers.length);
        var distItem = {
            leader:dist.leader
        };
        distItem.members = distMembers;
        distItem.districtId = dist.id;
        distConfig.push(distItem);
    }
    $.ajax({
        type: 'POST',
        url: "rest/district/create",
        data: JSON.stringify(distConfig),
        contentType: 'application/json;charset=UTF-8',
        success: function(data, textStatus, xhr){
        }
    });
}

function getMembersWithIn(leader, distance){
    var circle = new gm.Circle({
        center: new gm.LatLng(leader.lat, leader.lng),
        radius: distance
    });
    var members = [];
    for(var i=0; i<allAddresses.length; i++){
        var ad = allAddresses[i];
        if(ad.household === leader.household){
            // skip the leader
            continue;
        }
        if(isLeader(ad)){
            // skip leader of another district
            continue;
        }
        var ll = new gm.LatLng(ad.lat, ad.lng);
        if(gm.geometry.spherical.computeDistanceBetween(circle.getCenter(), ll) <= circle.getRadius()){
            members.push(ad);
        }
    }
    return members;
}

function isLeader(address){
   for(var i=0;i<districts.length; i++){
       var dl = districts[i].leader;
       var al = districts[i].assistant;
       if(address.household === dl?dl.household:undefined
       || address.household === al?al.household:undefined){
           return true;
       }
   }
   return false;
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

function getMembersInDistrict(dist){
    var radius = 500;
    var distMembers = getMembersWithIn(dist.leader, radius);
    while(distMembers.length < 25){
        radius = radius + 100;
        distMembers = getMembersWithIn(dist.leader, radius);
    }
    while(distMembers.length > 25){
        radius = radius - 100;
        distMembers = getMembersWithIn(dist.leader, radius);
    }
    return distMembers;
}