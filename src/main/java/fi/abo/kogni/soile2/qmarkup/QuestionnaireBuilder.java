package fi.abo.kogni.soile2.qmarkup;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

//import com.sun.org.apache.xpath.internal.operations.Bool;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import fi.abo.kogni.soile2.qmarkup.typespec.MalformedCommandException;
import fi.abo.kogni.soile2.qmarkup.typespec.Validator;
import fi.abo.kogni.soile2.utils.generator.UniqueStringGenerator;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;


public class QuestionnaireBuilder implements QuestionnaireProcessor {

	private JsonObject textStyle;
	private JsonObject blockStyle;
	private JsonObject currentTag;
    public QuestionnaireBuilder(URL template) {
        super();
        this.pendingTags = new ArrayDeque<>();
        this.tsStack = new ArrayDeque<>();
        this.bsStack = new ArrayDeque<>();
    //    this.group = template.getTemplate("questionnaire_proto.stg");
    //    this.group = template.getTemplate("questionnaire_embedded.stg");
        this.group = new STGroupFile(template);
        this.questionnaire = questionnaireST();
        this.spacer = group.getInstanceOf("vspacer");
        this.body = new JsonArray();
        this.validStmt = new JsonArray();
        this.currentParagraph = new JsonArray();
        this.currentElement = new JsonArray();
        Tag tag = Tag.newTag("p");
        this.paragraphOpenTag = tag.toString();
        this.paragraphCloseTag = Tag.closingTag(tag);
        this.questionnaireId = "";
        this.encryptionKey = "";
        this.currentLine = 0;
        this.inParagraph = false;
        this.textAsArgument = false;
        this.expectPageTitle = false;
        this.addSpacer = false;
        this.textStyle = new JsonObject();
        this.currentTag = new JsonObject();
        this.blockStyle = new JsonObject();
        this.currentStyle = new StringBuilder();
        AtomicInteger counter = UniqueStringGenerator.createCounter();
        this.idGen = new UniqueStringGenerator("id");
        this.idGen.setCounter(counter);
        this.nameGen = new UniqueStringGenerator("name");
        this.nameGen.setCounter(counter);
    }
    
    public QuestionnaireBuilder(String template) throws MalformedURLException
    {
    	this(QuestionnaireBuilder.class.getClassLoader().getResource(template));    	
    }
    public void questionnaireId(String id) {
        this.questionnaireId = id;
    }

    public String questionnaireId() {
        return this.questionnaireId;
    }

    public void encryptionKey(String key) {
        this.encryptionKey = key;
    }

    public String encryptionKey() {
        return this.encryptionKey;
    }

    private JsonObject newTag()
    {
    	return new JsonObject().put("type", "text").put("inline", true);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void processCommand(String command, ArrayList<String> args) throws MalformedCommandException {
        Tag tag = Tag.newTag(command);

        if (textAsArgument) {
            /*
             * We were probably expecting text, but
             * never mind. Proceed as usual, but close the pending tag.
             */
            textAsArgument = false;
            currentTag = newTag();
        }

        switch (command) {

        // Process those commands which take an object as argument.

        case "dropdownmenu":
            if (args.size() > 0) {
                //closeParagraph();
                Value value = Value.parse(args.get(0));
                Validator validator = Validator.validatorFor(command);
                validator.validate(value);
                ST tmpl = group.getInstanceOf(command);
                DropDownMenuWidgetData ddmwd = this.new DropDownMenuWidgetData();
                ValidateWidget vw = this.new ValidateWidget();
                String id = idGen.generate();
                ddmwd.setId(id);
                vw.setId(id);
                Boolean inline = ((BooleanValue) value.getValue("inline")).asBoolean();
                Boolean optional = ((BooleanValue) value.getValue("optional")).asBoolean();
                tmpl.add("id", id);
                String label = value.getValue("label").toString();
                if (label.isEmpty() == true) {
                    label = null;
                }
                tmpl.add("label", label);
                tmpl.add("required", (!optional));
                tmpl.add("inline", inline);
                String field = createColumnName(questionnaireId(),
                        value.getValue("dbcolumn").toString());
                ddmwd.setColumn(encrypt(field));
                Object o = value.getValue("options").asJavaObject();
                ArrayList<Value> options = (ArrayList<Value>) o;
                Iterator<Value> it = options.iterator();
                while (it.hasNext()) {
                    Value v = it.next();
                    tmpl.addAggr("options.{short, long}",
                                 v.getValue("dbvalue"), v.getValue("text"));
                }
                addWidget(tmpl.render(), inline);
                validationCode(ddmwd);
            }
            break;
        case "multiselect":
            if (args.size() > 0) {
                Value value = Value.parse(args.get(0));
                Validator validator = Validator.validatorFor(command);
                validator.validate(value);

                Boolean colalign = ((BooleanValue) value.getValue("colalign")).asBoolean();
                ST tmpl = group.getInstanceOf("select");
                String defaultValue = value.getValue("default_value").toString();
                ArrayList<Value> options =
                        (ArrayList<Value>)  value.getValue("options").asJavaObject();
                int numColumns = options.size();
//                int width = CONTENT_WIDTH_PX  / numColumns;
//                width -= 5;
                float width = 100/numColumns;

                MultiselectWidgetData mswd = this.new MultiselectWidgetData();
                mswd.setDefaultValue(defaultValue);
                String name = nameGen.generate();
                Iterator<Value> it = options.iterator();
                while (it.hasNext()) {
                    // Iterating over columns, basically.

                    Value c = it.next();
                    ST columnTmpl = group.getInstanceOf("selection_column");
                    if(colalign) {
                        columnTmpl.add("width", width);
                    } else {
                        columnTmpl.add("width", "");
                    }
                    ArrayList<Value> column = (ArrayList<Value>) c.asJavaObject();
                    Iterator<Value> iter = column.iterator();
                    while (iter.hasNext()) {
                        // Fill one column.

                        Value opt = iter.next();
                        ST optTmpl = group.getInstanceOf("checkbox");
                        String colname = createColumnName(questionnaireId(),
                                opt.getValue("dbcolumn").toString());
                        String id = idGen.generate();
                        optTmpl.add("id", id);
                        optTmpl.add("name", name);
                        String dbvalue = opt.getValue("dbvalue").toString().replace("\n", "");
                        optTmpl.add("value", dbvalue);
                        optTmpl.add("label", opt.getValue("text"));
                        Boolean checked = (Boolean) opt.getValue("checked").asJavaObject();
                        if (checked == false) {
                            checked = null;
                        }
                        optTmpl.add("checked", checked);
                        columnTmpl.addAggr("elems.{content}", optTmpl.render());
                        mswd.addId(id);
                        mswd.addColumn(encrypt(colname));
                        mswd.addValue(dbvalue);
                    }
                    //System.out.println(columnTmpl.render());
                    tmpl.addAggr("columns.{content}", columnTmpl.render());
                }
                validationCode(mswd);
                addWidget(tmpl.render());
            }
            break;
        case "numberfield":
            if (args.size() > 0) {
                Value value = Value.parse(args.get(0));
                Validator validator = Validator.validatorFor(command);
                validator.validate(value);

                Boolean inline = ((BooleanValue) value.getValue("inline")).asBoolean();
                ST tmpl = group.getInstanceOf(command);
                NumberFieldWidgetData nfwd = this.new NumberFieldWidgetData();
                String id = idGen.generate();
                tmpl.add("id", id);
                tmpl.add("name", nameGen.generate());

                String label = String.valueOf(value.getValue("label"));
                if(label.length()==0) {
                    tmpl.add("label", false);
                }else {
                    tmpl.add("label", label);
                }
                Value separator;
                if(value.getValue("separator") == null)
                {
                	separator = new StringValue("");
                }
                else
                {
                	separator = value.getValue("separator");
                }
                nfwd.setId(id);
                String field = createColumnName(questionnaireId(),
                        value.getValue("dbcolumn").toString());
                nfwd.setColumn(encrypt(field));
                Value min = value.getValue("minimum");
                Value max = value.getValue("maximum");
                Value incr = value.getValue("increment");
                //Can be empty if optional==true
                //Empty meaning that no value is set and that the field isn't required
                Boolean optional = ((BooleanValue) value.getValue("optional")).asBoolean();
                Integer startValue = ((IntegerValue) value.getValue("value")).asInteger();
                int width = max.toString().trim().length() + 2;
                tmpl.add("minimum", min);
                tmpl.add("maximum", max);
                tmpl.add("increment", incr);
                tmpl.add("width", width);
                tmpl.add("separator", separator);
                tmpl.add("inline", inline);
                if(startValue != 0) {
                    tmpl.add("value", startValue);
                } else {
                    tmpl.add("value", false);
                }
                tmpl.add("optional", optional);
                addWidget(tmpl.render(), inline);
                validationCode(nfwd);
            }
            break;
        case "singleselect":
            if (args.size() > 0) {
                Value value = Value.parse(args.get(0));
                Validator validator = Validator.validatorFor(command);
                validator.validate(value);
                //ST tmpl = group.getInstanceOf("select");
                ST tmpl = group.getInstanceOf("selectradio");
                SingleselectWidgetData sswd = this.new SingleselectWidgetData();
                String colname = createColumnName(questionnaireId(),
                        value.getValue("dbcolumn").toString());
                sswd.setColumn(colname);
                String defaultValue = value.getValue("default_value").toString();
                Boolean inline = ((BooleanValue) value.getValue("inline")).asBoolean();
                Boolean colalign = ((BooleanValue) value.getValue("colalign")).asBoolean();
                Boolean optional = ((BooleanValue) value.getValue("optional")).asBoolean();

                tmpl.add("optional", optional);

                if(!inline){
                    closeParagraph();
                }

                sswd.setDefaultValue(defaultValue);
                ArrayList<Value> options =
                        (ArrayList<Value>)  value.getValue("options").asJavaObject();
                int numColumns = options.size();
                //int width = CONTENT_WIDTH_PX  / numColumns;
                //width -= 5;
                float width = 100/numColumns;
                String name = nameGen.generate();
                Iterator<Value> optionsIt = options.iterator();
                while (optionsIt.hasNext()) {
                    // Iterating over columns, basically.

                    Value c = optionsIt.next();
                    ST columnTmpl = group.getInstanceOf("selection_column");
                    if (colalign) {
                        columnTmpl.add("width", width);
                    }
                    else {
                        columnTmpl.add("width", "");
                    }
                    ArrayList<Value> column = (ArrayList<Value>) c.asJavaObject();
                    Iterator<Value> iter = column.iterator();
                    while (iter.hasNext()) {
                        // Fill one column.

                        Value opt = iter.next();
                        ST optTmpl = group.getInstanceOf("radiobutton");
                        String id = idGen.generate();
                        optTmpl.add("id", id);
                        optTmpl.add("name", name);
                        String dbvalue = opt.getValue("dbvalue").toString().replace("\n", "");
                        optTmpl.add("value", dbvalue);
                        optTmpl.add("label", opt.getValue("text").toString().replace("\n", ""));
                        Boolean checked = (Boolean) opt.getValue("checked").asJavaObject();
                        if (checked == false) {
                            checked = null;
                        }
                        optTmpl.add("checked", checked);
                        //optTmpl.add("optional", optional);
                        columnTmpl.addAggr("elems.{content}", optTmpl.render());
                        sswd.addId(id);
                        sswd.addValue(dbvalue);
                    }
                    tmpl.addAggr("columns.{content}", columnTmpl.render());
                }
                validationCode(sswd);
                addWidget(tmpl.render());
            }
            break;
        case "slider":
            if (args.size() > 0) {
                closeParagraph();
                Value value = Value.parse(args.get(0));
                Validator validator = Validator.validatorFor(command);
                validator.validate(value);
                ST tmpl = group.getInstanceOf(command);
                SliderWidgetData swd = this.new SliderWidgetData();
                String id = idGen.generate();
                tmpl.add("id", id);
                swd.setId(id);
                Integer min = (Integer) value.getValue("minimum").asJavaObject();
                Integer max = (Integer) value.getValue("maximum").asJavaObject();
                Integer incr = (Integer) value.getValue("increment").asJavaObject();
                Integer select = (Integer) value.getValue("select").asJavaObject();                
                String style; 
                try {
                	if(value.getValue("style") == null)
                	{
                		style = "width = 380px";
                	}
                	else
                	{
                		style = (String) value.getValue("style").asJavaObject();
                		tmpl.add("style", style);
                	}
                }
                catch(Exception e)
                {
                	e.printStackTrace(System.out);
                }                
                tmpl.add("minimum", min);
                tmpl.add("maximum", max);
                tmpl.add("increment", incr);
                tmpl.add("select", select);
                String field = createColumnName(questionnaireId(),
                        value.getValue("dbcolumn").toString());
                swd.setColumn(encrypt(field));
                Object o = value.getValue("labels").asJavaObject();
                ArrayList<Value> labels = (ArrayList<Value>) o;
                int count = labels.size();
                Iterator<Value> it = labels.iterator();
                while (it.hasNext()) {
                    tmpl.addAggr("labels.{value}", it.next());
                }
                tmpl.add("count", count);
                addWidget(tmpl.render());
                validationCode(swd);
            }
            break;
        case "textarea":
            if (args.size() > 0) {
                closeParagraph();
                Value value = Value.parse(args.get(0));
                Validator validator = Validator.validatorFor(command);
                validator.validate(value);
                ST tmpl = group.getInstanceOf(command);
                String id = idGen.generate();
                TextareaWidgetData tawd = this.new TextareaWidgetData();
                String field = createColumnName(questionnaireId(),
                        value.getValue("dbcolumn").toString());
                Value maxlen = value.getValue("length");
                Boolean required = false;
                Value separator;
                if(value.getValue("separator") == null)
                {
                	separator = new StringValue("");
                }
                else
                {
                	separator = value.getValue("separator");
                }
                required = ((BooleanValue) value.getValue("optional")).asBoolean();
                tmpl.add("id", id);
                tmpl.add("rows", value.getValue("rows"));
                tmpl.add("columns", value.getValue("columns"));
                tmpl.add("text", value.getValue("text"));
                tmpl.add("label", value.getValue("label"));
                tmpl.add("maxlength", maxlen);
                tmpl.add("required", required);
                tawd.setId(id);
                tawd.setColumn(encrypt(field));
                tmpl.add("separator", separator);
                addWidget(tmpl.render());
                validationCode(tawd);
            }
            break;
        case "textbox":
            if (args.size() > 0) {
                Value value = Value.parse(args.get(0));
                Validator validator = Validator.validatorFor(command);
                validator.validate(value);
                ST tmpl = group.getInstanceOf(command);
                String id = idGen.generate();
                TextboxWidgetData tbwd = this.new TextboxWidgetData();
                boolean linebreak = false;
                if(value.getValue("linebreak") != null)
                {
                	linebreak = ((BooleanValue) value.getValue("linebreak")).asBoolean();
                }
                String field = createColumnName(questionnaireId(),
                        value.getValue("dbcolumn").toString());
                String label = value.getValue("label").toString();
                Boolean required = false;
                required = !((BooleanValue) value.getValue("optional")).asBoolean();
                if (label.isEmpty() == true) {
                    label = null;
                }
                tmpl.add("id", id);
                tmpl.add("inline", true);
                tmpl.add("label", label);
                Value maxlen = value.getValue("length");
                tmpl.add("length", maxlen);
                tmpl.add("text", value.getValue("text"));
                tmpl.add("required", required);
                tbwd.setId(id);
                tbwd.setColumn(encrypt(field));
                addWidget(tmpl.render(), true);
                if(linebreak)
                {
                	addText("\n");
                }                
                validationCode(tbwd);
            }
            break;

        // Process other commands (with simple arguments).

        case "bs":          // "Block Style"
            if (args.size() > 0) {
                closeParagraph();
                workOnStack(args, blockStyle);
            }
            break;
        case "lb":          // "Line Break"
            addText("\n");            
            break;
        case "link":        	
        	currentTag = new JsonObject().put("inline", true);
        	currentTag.put("type", "link");        	
            if (args.size() >= 1) {
                currentTag.put("href", args.get(0));                                
                textAsArgument = true;
            }
            if (args.size() == 2) {
            	currentTag.put("class", args.get(1));
                textAsArgument = true;
            }
            break;
        case "p":           // "Paragraph"
            closeParagraph();
            
            inParagraph = true;
            //addTag(tag);
            break;
        case "pagetitle":
            textAsArgument = true;
            expectPageTitle = true;        	            
            break;
        case "pb":          // "Paragraph Break"
            closeParagraph();
            break;
        case "subtitle":
        	closeParagraph();
        	currentTag = new JsonObject().put("type", "subtitle");            
            textAsArgument = true;                                           
            break;
        case "title":
        	closeParagraph();
        	currentTag = new JsonObject().put("type", "title");            
            textAsArgument = true;            
            break;
        case "ts":          // "Text Style"            
            workOnStack(args, textStyle);            
            break;
        case "spacer":
            closeParagraph();
            addSpacer = true;
            break;
        case "if":
            break;
        case "elseif":
            break;
        case "else":
            break;
        case "endif":
            break;
        default:
            String msg = String.format("Unknown command: '%s'.", command);
            throw new MalformedCommandException(msg);
        }
    }

    @Override
    public void processText(String text) {
    	
        if (expectPageTitle) {
            questionnaire.add("title", text);
            expectPageTitle = false;
            textAsArgument = false;
            return;
        }
        // This should probably be a StringBuilder, but it might just work ok...
        addText(text);
        if (textAsArgument) {
            closeTag();
            textAsArgument = false;
        }
    }
    
    private void closeTag()
    {
    	if(textStyle.isEmpty())
    	{
    		currentTag.put("style", new JsonObject().mergeIn(textStyle));
    	}
    	else
    	{
    		currentTag.put("style", new JsonObject().mergeIn(blockStyle));
    	}
    	String tagType = addAndClearCurrentElement(currentParagraph);
    	if(tagType.equals("subtitle") || tagType.equals("title") )
    	{
    		closeParagraph();
    	}
    }
    

    private void addText(String text)
    {
    	currentTag.put("text",currentTag.getString("text","") + text);
    }
    
    
    @Override
    public void finish() {
        closeParagraph();
        emptyBsStack();
    }

    @Override
    public String output() {    	
        questionnaire.add("elements", body.encodePrettily());
        questionnaire.add("functions", validStmt.encodePrettily());
        return questionnaire.render();
    }

    public void reset() {
        pendingTags.clear();
        tsStack.clear();
        bsStack.clear();
        body = new JsonArray();
        validStmt = new JsonArray();
//        questionnaire.remove("body");
//        questionnaire.remove("stmts");
//        questionnaire.remove("title");
        questionnaire = questionnaireST();
        inParagraph = false;
        textAsArgument = false;
        addSpacer = false;
        questionnaireId = "";
    }


    private void emptyTsStack() {
    	textStyle.clear();
    }

    private void emptyBsStack() {
    	blockStyle.clear();
    }

    private void closeParagraph() {
        if (inParagraph) {
            emptyTsStack();
            //addTag(paragraphCloseTag);
            inParagraph = false;
        }
        addAndClearCurrentElement(currentParagraph);
        if(currentParagraph.size() > 0)
        {
        	body.add(currentParagraph);
        }
        currentParagraph = new JsonArray();

    }
    private void addWidget(String WidgetJson)
    {
    	addWidget(WidgetJson, false);
    }

    private void addWidget(String WidgetJson, boolean inline)
    {
    	//Add the widget to the current paragraph.
    	JsonArray current;
		    		
    	if(inParagraph || inline)
    	{
    		current = currentParagraph;
    	}
    	else
    	{
    		// if we are not in the paragraph, we need to close whatever paragraph we have and
    		// start a new one.
    		closeParagraph();
    		current = new JsonArray();
    		body.add(current);
    	}    	
		addAndClearCurrentElement(current);    	
    	JsonObject widget = new JsonObject(WidgetJson);
		current.add(new JsonObject().put("type", widget.getString("type")).put("data",widget));
    }
        
    private String addAndClearCurrentElement(JsonArray target)
    {
    	// add only if this actually is non empty
    	if(!currentTag.getString("text","").equals(""))
    	{
    		target.add(new JsonObject().put("type", "html").put("data", currentTag));
    		
    	}
    	String type = currentTag.getString("type","");
    	currentTag = newTag();
    	return type;
    }

    private void workOnStack(ArrayList<String> args, JsonObject styleObject) {
        String arg = decodeStackArg(args.get(0));

        switch (arg) {
        case "push":
        	JsonObject newStyle = new JsonObject();
            if (args.size() > 1) {
            	            	                
                for (int i = 1; i < args.size(); ++i) {                	
                    String a = args.get(i);
                    if (TextStyle.defined(a)) {
                        TextStyle current = TextStyle.get(a);
                        newStyle.put(current.getType(), current.getValue());                        
                    }
                }                
            }
            styleObject.mergeIn(newStyle);            
            break;

        case "pop":
        	if (args.size() > 1) {                
                for (int i = 1; i < args.size(); ++i) {                	
                    String a = args.get(i);
                    if (TextStyle.defined(a)) {
                        TextStyle current = TextStyle.get(a);
                        if(styleObject.getString(current.getType(),"").equals(current.getValue()))
                        {
                        	styleObject.remove(current.getType());
                        }
                    }
                }                
            }
            break;

        case "empty":
            styleObject.clear();
            break;
        default:
            String msg = String.format("Unknown stack argument: '%s'", arg);
            throw new MalformedCommandException(msg);
        }
    }

    private String decodeStackArg(String arg) {
        if (arg.length() == 1) {
            if (arg.charAt(0) == '+') {
                return "push";
            }
            if (arg.charAt(0) == '-') {
                return "pop";
            }
            if (arg.charAt(0) == '!') {
                return "empty";
            }
        }
        return arg;
    }

    private String createColumnName(String qid, String col) {
        col = col.replace(".", "").replace("\n", "");
        int len = qid.length() + col.length() + 1;
        return col;
        /*StringBuilder sb = new StringBuilder(len);
        sb.append(qid);
        //sb.append(':');
        sb.append(col);
        return sb.toString();*/
    }

    private void validationCode(QuestionnaireWidget data) {
        validationCode(data.render());
    }

    private void validationCode(String code) {
        validStmt.addAll(new JsonArray(code));        
    }

    private String encrypt(String field) {
        // TODO Really encrypt the field.
        //return Blowfish.encrypt(field, encryptionKey());
        return field;
    }

    private ST questionnaireST() {
        return group.getInstanceOf("questionnaire_html");
    }

    private ArrayDeque<String> pendingTags;
    private ArrayDeque<Tag> tsStack;
    private ArrayDeque<Tag> bsStack;

    // The body of the questionnaire
    private JsonArray body;

    // Validation statements.
    private JsonArray validStmt;

    // The current paragraph
    private JsonArray currentParagraph;

    private int currentLine;
    
    private STGroup group;
    private ST spacer;
    private ST questionnaire;
    private String paragraphOpenTag;
    private String paragraphCloseTag;
    private String questionnaireId;
    private String encryptionKey;
    private boolean expectPageTitle;
    private boolean inParagraph;
    private boolean textAsArgument;
    private boolean addSpacer;
    private UniqueStringGenerator idGen;
    private UniqueStringGenerator nameGen;
    private JsonArray currentElement;
    private StringBuilder currentStyle;
    

    private abstract class QuestionnaireWidget {
        public QuestionnaireWidget(String n) {
            this.tmpl = group.getInstanceOf(n);
        }

        protected ST getTmpl() {
            return tmpl;
        }

        public String render() {
            return getTmpl().render();
        }

        private ST tmpl;
    }

    private class ValidateWidget extends QuestionnaireWidget {
        public ValidateWidget() {
            super("qdata_validate_widget");
        }

        public ValidateWidget setId(String id) {
            getTmpl().add("id", id);
            return this;
        }
    }

    private class NonemptyTextWidget extends QuestionnaireWidget {
        public NonemptyTextWidget() {
            super("qdata_nonempty_text_widget");
        }

        public NonemptyTextWidget setId(String id) {
            getTmpl().add("id", id);
            return this;
        }
    }

    private class DropDownMenuWidgetData extends QuestionnaireWidget {
        public DropDownMenuWidgetData() {
            super("qdata_widgetdata_dropdownmenu");
        }

        public void setId(String id) {
            getTmpl().add("id", id);
        }

        public void setColumn(String col) {
            getTmpl().add("column", col);
        }

    }

    private class SingleselectWidgetData extends QuestionnaireWidget {
        public SingleselectWidgetData() {
            super("qdata_widgetdata_singleselect");
        }

        public void addId(String id) {
            getTmpl().addAggr("ids.{text}", id);
        }

        public void setColumn(String col) {
            getTmpl().add("columnName", col);
        }

        public void addValue(String col) {
            getTmpl().addAggr("values.{text}", col);
        }

        public void setDefaultValue(String dw) {
            getTmpl().add("defaultValue", dw);
        }

    }

    private class MultiselectWidgetData extends QuestionnaireWidget {
        public MultiselectWidgetData() {
            super("qdata_widgetdata_multiselect");
        }

        public void addId(String id) {
            getTmpl().addAggr("ids.{text}", id);
        }

        public void addColumn(String col) {
            getTmpl().addAggr("columns.{text}", col);
        }

        public void addValue(String col) {
            getTmpl().addAggr("values.{text}", col);
        }

        public void setDefaultValue(String dw) {
            getTmpl().add("defaultValue", dw);
        }

    }

    private class NumberFieldWidgetData extends QuestionnaireWidget {
        public NumberFieldWidgetData() {
            super("qdata_widgetdata_numberfield");
        }

        public void setId(String id) {
            getTmpl().add("id", id);
        }

        public void setColumn(String col) {
            getTmpl().add("columnName", col);
        }

    }

    private class SliderWidgetData extends QuestionnaireWidget {
        public SliderWidgetData() {
            super("qdata_widgetdata_slider");
        }

        public void setId(String id) {
            getTmpl().add("id", id);
        }

        public void setColumn(String col) {
            getTmpl().add("columnName", col);
        }

    }

    private class TextareaWidgetData extends QuestionnaireWidget {
        public TextareaWidgetData() {
            super("qdata_widgetdata_textarea");
        }

        public void setId(String id) {
            getTmpl().add("id", id);
        }

        public void setColumn(String col) {
            getTmpl().add("columnName", col);
        }

        public void setMaxLength(String len) {
            getTmpl().add("maxlength", len);
        }
    }

    private class TextboxWidgetData extends QuestionnaireWidget {
        public TextboxWidgetData() {
            super("qdata_widgetdata_textbox");
        }

        public void setId(String id) {
            getTmpl().add("id", id);
        }

        public void setColumn(String col) {
            getTmpl().add("columnName", col);
        }

        public void setMaxLength(String len) {
            getTmpl().add("maxlength", len);
        }
    }

    private static final int CONTENT_WIDTH_PX = 600;

}
