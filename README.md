
[![Build Status](https://github.com/Abo-Soile/SOILE-QuestionaireMarkup/workflows/CI-CD/badge.svg)](https://github.com/Abo-Soile/SOILE-elang/workflows/CI-CD/badge.svg)
[![codecov](https://codecov.io/gh/Abo-Soile/SOILE-QuestionaireMarkup/branch/main/graph/badge.svg?token=1H9DR2CR7S)](https://codecov.io/gh/Abo-Soile/SOILE-elang)
[![License (LGPL version 3)](https://shields.io/badge/license-MIT-informational)](https://shields.io/badge/license-MIT-informational)

# SOILE-QuestionaireMarkup
Questionaire Markup Language for the Soile Web service

# Installation
```
git clone git@github.com:Abo-Soile/SOILE-QuestionaireMarkup.git
cd SOILE-QuestionaireMarkup
mvn install
```
# Create Documentation
```
mvn site
```

# Output

The QMarkup Builder converts the given input questionnaire code into a JSON object that can be used to build the layouted questionnaire.
The resulting output has the following structure:
```json
{
 "title" : "The title the resulting page should have (page title, not Section title)",
  "elements" :  [ // one element represents one paragraph, i.e. a new paragraph should be started after each element of the elements array
        [ {
        "type" : "dropdownmenu",
        "data" : {
            "type" : "dropdownmenu",
            "inline" : false,
            "label" : "Select Continent",
            "id" : "id_7fffffff",
            "required" : "true",
            "options" : [ {
            "value" : "EU",
            "text" : "Europe"
            }, {
            "value" : "AM",
            "text" : "America"
            } ]
        }
        } ]
 , [ {
  "type" : "select", // Can only select one.
  "data" : {
    "type" : "select",
    "options" : [ {
      "type" : "checkbox",
      "id" : "id_7ffffffd",
      "name" : "name_7ffffffe",
      "label" : "Bike",
      "checked" : true,
      "value" : "yes"
    }, {
      "type" : "checkbox",
      "id" : "id_7ffffffc",
      "name" : "name_7ffffffe",
      "label" : "Car",
      "checked" : false,
      "value" : "yes"
    } ]
  }
} ], 
[ {
  "type" : "numberfield", // a number that needs to be selected if optional is false
  "data" : {
    "type" : "numberfield",
    "id" : "id_7ffffffa",
    "name" : "name_7ffffff9",
    "label" : "Enter your age",
    "value" : false,
    "width" : 5,
    "minimum" : 0,
    "maximum" : 100,
    "optional" : true,
    "increment" : 1,
    "separator" : "",
    "inline" : true
  }
} ],
[ {
  "type" : "selectradio", // radio select, only one selection allowed
  "data" : {
    "type" : "selectradio",
    "options" : [ {
      "type" : "radiobutton",
      "id" : "id_7ffffff7",
      "name" : "name_7ffffff8",
      "label" : "Yes",
      "checked" : true,
      "value" : "yes"
    }, {
      "type" : "radiobutton",
      "id" : "id_7ffffff6",
      "name" : "name_7ffffff8",
      "label" : "No",
      "checked" : false,
      "value" : "no"
    }, {
      "type" : "radiobutton",
      "id" : "id_7ffffff5",
      "name" : "name_7ffffff8",
      "label" : "Maybe",
      "checked" : false,
      "value" : "maybe"
    } ],
    "optional" : true
  }
} ], 
[ {
  "type" : "slider", // slider with value range and labels for the values.
  "data" : {
    "type" : "slider",
    "id" : "id_7ffffff4",
    "labels" : [ "0", "1", "2", "3", "4", "5", "6" ],
    "minimum" : 0,
    "maximum" : 6,
    "increment" : 1,
    "count" : 7,
    "select" : 0
  }
} ], 
[ {
  "type" : "textarea", // larger text area to write in
  "data" : {
    "type" : "textarea",
    "id" : "id_7ffffff3",
    "label" : "Tell us a story",
    "text" : "Enter your story here.",
    "rows" : 3,
    "columns" : 80,
    "required" : true,
    "separator" : ""
  }
} ], 
[ {
  "type" : "textbox", // individual text area, can be inline,
  "data" : {
    "type" : "textbox",
    "id" : "id_7ffffff2",
    "label" : "Enter you name",
    "length" : 40,
    "text" : "Enter your name here",
    "required" : false,
    "inline" : true
  }
}, 
{
  "type" : "html", // general text/html has a subtype
  "data" : {
    "inline" : true,
    "type" : "link", // link, should open a new window when clicked
    "href" : "http://some.address.on.the.web/",
    "text" : "Text for the link",
    "style" : { }
  }
} ],
[ {
  "type" : "html",
  "data" : {
    "type" : "text", // general text.
    "inline" : true,
    "text" : "Here you have to select a number",
    "style" : {"Additional Style elements for the text"}
  }
}, {
  "type" : "numberfield",
  "data" : {
    "type" : "numberfield",
    "id" : "id_7ffffe39",
    "name" : "name_7ffffe38",
    "value" : false,
    "width" : 4,
    "minimum" : 0,
    "maximum" : 24,
    "optional" : true,
    "increment" : 1,
    "separator" : "",
    "inline" : true
  }
}, {
  "type" : "html",
  "data" : {
    "type" : "text",
    "inline" : true,
    "text" : " and get some text after the number"
  }
} ], [ {
  "type" : "title",
  "data" : {
    "type" : "text",
    "inline" : true,
    "text" : "This is some new title"
  }
} ],
[ {
  "type" : "subtitle",
  "data" : {
    "type" : "text",
    "inline" : true,
    "text" : "And a subtitle"
  }
} ]
 ],
"mapping" : [ {
  "id" : "id_7fffffff",
  "dbcolumn" : "continent",
  "value" : null,
  "defaultValue" : null
}, {
  "id" : "id_7ffffffd",
  "dbcolumn" : "Bike",
  "value" : "yes",
  "defaultValue" : "no"
}, {
  "id" : "id_7ffffffc",
  "dbcolumn" : "Car",
  "value" : "yes",
  "defaultValue" : "no"
}, {
  "id" : "id_7ffffffb",
  "dbcolumn" : "Motorbike",
  "value" : "yes",
  "defaultValue" : "no"
}, {
  "id" : "id_7ffffffa",
  "dbcolumn" : "age",
  "value" : null,
  "defaultValue" : null
}, {
  "id" : "id_7ffffff7",
  "dbcolumn" : "Yes/No/maybe",
  "value" : "yes",
  "defaultValue" : "-1"
}, {
  "id" : "id_7ffffff6",
  "dbcolumn" : "Yes/No/maybe",
  "value" : "no",
  "defaultValue" : "-1"
}, {
  "id" : "id_7ffffff5",
  "dbcolumn" : "Yes/No/maybe",
  "value" : "maybe",
  "defaultValue" : "-1"
}, {
  "id" : "id_7ffffff4",
  "dbcolumn" : "Slider 0-6",
  "value" : null,
  "defaultValue" : null
}, {
  "id" : "id_7ffffff3",
  "dbcolumn" : "story",
  "value" : "",
  "defaultValue" : ""
}, {
  "id" : "id_7ffffff2",
  "dbcolumn" : "name",
  "value" : "",
  "defaultValue" : ""
} ]     
}
}
```
The mapping array contains all mappings between ids of elements and the respective targets this will be stored in.
This also allows to easily extract possible outputs of the task. IDs mapping to the same column are assumed to overwrite 
the existing value when selected. 


