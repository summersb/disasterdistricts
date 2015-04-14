# Introduction #

This page walks through the process of loading data into the system


# Details #

You need to have you data in a CSV file in the following format with a header row.  The order of columns is required, there can be any number of additional columns but they are ignored.

```
"Family Name","Couple Name","Family Phone","Family Email","Family Address","Head Of House Name","Head Of House Phone","Head Of House Email",...
```

The fields that are utilized are:
  * Family Name -- this must be unique
  * Family Phone
  * Family Address -- this address must resolve to a single address via google apis
  * Head of House Email

Once you have this file you need to run the Cvs2Json class using your CSV as a source file
This will create a members.js file.

**You will need to edit the members.js file and remove the trailing comma**

This file needs to be inserted into the database by running the LoadMemberList file

Once this is completed you should be able to see your families in the application