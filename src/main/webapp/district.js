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
function initialize() {
    $.getJSON('rest/district/list', function(data) {
        show(data);
        memberList = data;
    });
    $.getJSON('rest/member/list', function(data) {
        updateLeaders(data);
        memberList = data;
    });
}

function show(district) {
    body = '<table class="sortable" border="1" id="table"><thead><tr><th>District ID</th><th>Leader</th><th>Assistant Leader</th></tr></thead><tbody>';
    for (var i = 0; i < district.length; i++) {
        body += '<tr><td>' + district[i].id
                + '</td><td>' + district[i].leader.household
                + '</td><td>' + district[i].assistant.household
                + '</td></tr>';
    }
    body += '</tbody></table>';
    $('#body').append(body);
    var table = document.getElementById("table");
    sorttable.makeSortable(table);
}

function createDistrict() {
    var id = $('#createdistid').val();
    var district = '{"id":' + id + ',"leader":' + $('#distLeader').val() 
            + ',"assistant":' + $('#distAssistLeader').val() + '}';
//    var district = '{"id":' + id + '"}';
    $.ajax({
        type: "POST",
        url: "rest/district",
        data: JSON.stringify(district),
        contentType: 'application/json;charset=UTF-8',
        success: function(data, textStatus, xhr) {
            $('#body').empty();
            initialize();
        }
    });
}

function updateLeaders(address) {
    address.sort(sortAddresses);
    for (var i = 0; i < address.length; i++) {
        body += '<option value=\'' + JSON.stringify(address[i])
                + '\'>' + address[i].household
                + '</option>';
    }
    body += '</tbody></table>';
    $('#distLeader').append(body);
    $('#distAssistLeader').append(body);
}

function sortAddresses(addressA, addressB) {
    return addressA.household.localeCompare(addressB.household);
}

//function createDistrict() {
//    $.ajax({
//        type: "POST",
//        url: "rest/district",
//        data: JSON.stringify(dist),
//        contentType: 'application/json;charset=UTF-8',
//        success: function(data, textStatus, xhr) {
//            alert("saved address");
//        }
//    });
//}