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
var memberList = [];
function initialize(){
    $.getJSON('rest/member/list', function(data){
        show(data);
        memberList = data;
    });
}

function show(address){
    body = '<table class="sortable" border="1" id="table"><thead><tr><th>District ID</th><th>Household</th><th>Address</th><th>Lat</th><th>Long</th><th>Update</th></tr></thead><tbody>';
    for(var i=0; i<address.length; i++){
        body += '<tr><td>' + address[i].district
        + '</td><td>' + address[i].household
        + '</td><td><input id="'
        + address[i].household
        + '" size=75 type="text" name="address" value="' + address[i].address
        + '"></input></td><td>' + address[i].lat
        + '</td><td>' + address[i].lng
        + '</td><td><input type="button" value="Update" onclick="updateAddress(\''
        + address[i].household
        + '\', document.getElementById(\'' + address[i].household + '\'))"/>'
        + '</td></tr>';
    }
    body += '</tbody></table>';
    $('#body').append(body);
    var table = document.getElementById("table");
    sorttable.makeSortable(table);
}

function updateAddress(household, input){
    for (var i = 0; i < memberList.length; i++) {
        if(memberList[i].household === household){
            memberList[i].address = input.value;
            $.ajax({
                type: "PUT",
                url: "rest/member",
                data: JSON.stringify(memberList[i]),
                contentType: 'application/json;charset=UTF-8',
                success: function(data, textStatus, xhr){
                    alert("saved address");
                }
            });
        }
    }
}