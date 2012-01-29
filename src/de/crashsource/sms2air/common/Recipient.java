package de.crashsource.sms2air.common;

/**
 * 
 * @author Fabian Mueller, .crashsource, 2010
 * 
 */
public class Recipient extends Person implements Cloneable {
	public static final int NO_TYPE = -1;
	public static final int NO_RETURN_CODE = -1;

	/**
	 * The number of the recipient.
	 */
	protected String number;

	/**
	 * After the SMS was sent use this variable to link to the return code of
	 * the SMS provider. Use <code>NO_RETURN_CODE</code> if there is no code
	 * returned yet.
	 */
	protected int returnCode;

	/**
	 * This variable links to the Android type of the given phone number. Use
	 * <code>NO_TYPE</code> if there is no Type.
	 */
	protected int numberType;

	public Recipient(String lookupKey, String name, String number,
			int numberType, int returnCode) {
		super(lookupKey, name);
		this.number = number;
		this.numberType = numberType;
		this.returnCode = returnCode;
	}

	public Recipient(Person person, String number, int numberType,
			int returnCode) {
		super(person);
		this.number = number;
		this.numberType = numberType;
		this.returnCode = returnCode;
	}

	@Override
	protected Recipient clone() throws CloneNotSupportedException {
		return new Recipient(this.getLookupKey(), this.getName(), this.number,
				this.numberType, this.returnCode);
	}
	
	public String getNumber() {
		return number;
	}
	
	public int getNumberType() {
		return numberType;
	}
	
	public int getReturnCode() {
		return returnCode;
	}
	
	public void setNumber(String number) {
		this.number = number;
	}
	
	public void setNumberType(int numberType) {
		this.numberType = numberType;
	}
	
	public void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}
	
	@Override
	public String toString() {
		String ret = "";
		ret += "Lookup-Key: " + this.lookupKey + "\n";
		ret += "Name: " + this.name + "\n";
		ret += "Number: " + this.number + "\n";
		ret += "Number Type:" + this.numberType + "\n";
		ret += "Return Code: " + this.returnCode + "\n";
		return ret;
	}
}
