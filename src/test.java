
public class test 
{
	public static void main(String[] args) 
	{
		String username = "Cool username";
		String[] nameArray = username.split(" ");
		if(nameArray[0] == "Cool")
		{
			System.out.println("You are one 'Cool' cookie.");
		}
		else{System.out.println("didnt work");
		System.out.println(nameArray[0] + "1");}
		
		String myString = null;
		try{
			myString += "test";
			System.out.println(myString);
			System.out.println(myString.equals(" "));
			myString = null;
		}catch(NullPointerException e){
			System.out.println("No string to use");
		}
	}
}
