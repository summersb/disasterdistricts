# Introduction #

This page will describe the steps to create a disaster district list


# Details #

Once the you have your members loaded you can click on one and click on the Create District link to create this household as a district leader.

District leaders have a red circle of 500m in diameter, assistants have a green circle of the same size.

Once you have enough leaders selected click the District button at the top.

This may take some time as it is computing the shortest distance between each household and each leader.

Once this completes you should not have any households with a -1 district number.  If you do then you need to create more districts.

Once a household is assigned to a district you can make the assistants.  Click on the household and select Assign as Assistant.

You can now open http://localhost:8080/disasterlocator/rest/member/csv
to get a list of households by district.  Import this file into excel and format how you like

host mode url:

http://localhost:8080/disasterlocator/index.html?gwt.codesvr=localhost:9997