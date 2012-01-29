package de.crashsource.sms2air.common;

/**
 * 
 * @author Fabian Mueller, .crashsource, 2010
 * 
 */
public class Person {
	protected String lookupKey;
	protected String name;
	
	public Person(String lookupKey, String name) {
		this.lookupKey = lookupKey;
		this.name = name;
	}
	
	public Person(Person person) {
		this.lookupKey = person.getLookupKey();
		this.name = person.getName();
	}
	
	public String getName() {
		return name;
	}
	
	public String getLookupKey() {
		return lookupKey;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setLookupKey(String lookupKey) {
		this.lookupKey = lookupKey;
	}
	
	@Override
	public String toString() {
		return "Person: lookupKey: " + this.lookupKey + "; name: " + this.name;
	}
}
