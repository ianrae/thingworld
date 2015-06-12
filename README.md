Twixt is an alternative binding framework for Java Play 2.3.x apps.

The Play Framework documentation recommends that forms should be made from model objects.  By doing this, the model's validation annotations are used during binding.  This works well in simple CRUD scenarios, but has limitations in larger applications:

   * the form may only have a subset of model fields.  Binding may fail due to missing @Required fields.
   * the same field may be used on multiple forms with different validation requirements.
   * a form may contain fields from multiple models.

Twixt helps you provide an object (called a twixt) that sits between the model and the view.  The fields in a twixt object have their own validation and formatting, separate  from the model.  Twixt provides an automatic mechanism for copying data to and from a model. Let's looks at typical edit action:

      User user = User.find().getById(id); //get model
      UserTwixt twixt = new UserTwixt();   //create twixt
      twixt.copyFromModel(user);           //load twixt
      Form<UserTwixt> form = Form.form(UserTwixt.class).fill(twixt);
      //..render the form

*copyFromModel* copies fields automatically, or can be overridden to do custom copying. 

On post-back, use a **TwixtBinder** to bind the data in the HTTP request to your **TwixtForm**. 

      TwixtBinder<UsreTwixt> binder = new TwixtBinder();
      if (! binder.bind()) {
        Form<UserTwixt> form = Form.form(UserTwixt.class).fill(binder.get());
        //render the form again with validation errors and user's input data
      } else {
        UserTwixt twixt = binder.get();
        User user = User.find().getById(id);
        twixt.copyToModel(user);  //copy data back to model
        user.update();
      }

## Value Objects
Each twixt class consists of public fields that are **Value objects**.  A value class contains its own validation and formatting.Twixt provides BooleanValue, IntegerValue, StringValue, ListValue, and others.  However, you are encouraged to create value classes for your domain objects, such as: email, phone number, person name, and zip code.  Here is a phone number class:

	  public class PhoneNumberValue extends StringValue
	  {
		 class PhoneValidator {
			public void validate(ValContext vtx, Value val) {
			 Pattern p = Pattern.compile("^(?=.{7,32}$)(\\(?\\+?[0-9]*\\)?)?[0-9_\\- \\(\\)]*((\\s?x\\s?|ext\\s?|extension\\s?)\\d{1,5}){0,1}$");  
			 String phone = val.toString();
			 boolean matchFound = p.matcher(phone).matches();
			 if (! matchFound) {
				vtx.addError("Not a valid phone number.");
			 }
			}
		 }

		 public PhoneNumberValue()
		 {
			super();
			setValidator(new PhoneValidator());
		 }
	  }
	  
Validation is code-based, not annotation-based.	  

## Views
Use twixt objects in your controllers.  The view renders each value field in the normal way.
For example, if the twixt form had a StringValue field named <b>name</b>, render
it in the form like this:

    @inputText(sampleForm("name"))

## Automatic CRUD Controllers with play-crud
Twixt integrates with play-crud (https://github.com/hakandilek/play2-crud) to provide automatic CRUD.  
Play2-crud creates controllers automatically for any model derived from its **BasicModel** class.

Play2-crud can be used to give your application CRUD abilities, or you can integrate Twixt with Play2-crud.
This can be done in two ways.

### Dynamic CRUD 
The simplest approach is to create a controller based on **DynamicTwixtController**, telling it which model and twixt to use.  This controller defines standard CRUD actions (index, newForm, create, edit, update, show, and index), and views for each.  You may override the views.

### Custom Controller and View
To extend or customize, create a controller class based on **TwixtController**. It defines the same CRUD actions as **DynamicTwixtController**. You must define the views for each.


   
   
   
   
   
   
