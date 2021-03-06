package code;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

/**
 * A DocumentFilter to ensure only positive integers can be entered into a text
 * field.
 * 
 * Based on the StackOverflow answer at
 * "https://stackoverflow.com/questions/11093326/restricting-jtextfield-input-to-integers?answertab=votes#tab-top"
 * 
 * @author Luke
 *
 */
public class IntFilter extends DocumentFilter {
	/*
	 * Invoked prior to insertion of text into the specified Document. Allows us
	 * to conditionally allow insertion.
	 */
	@Override
	public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) {

		Document doc = fb.getDocument();
		StringBuilder sb = new StringBuilder();
		try {
			sb.append(doc.getText(0, doc.getLength()));
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		sb.insert(offset, string);

		if (test(sb.toString())) {
			try {
				super.insertString(fb, offset, string, attr);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param text
	 *            The text to test
	 * @return True if the text is valid (a positive integer)
	 */
	private boolean test(String text) {
		// Allow the text to be empty
		if (text.trim().isEmpty()) {
			return true;
		}
		// Only allow up to 5 digits
		if (text.trim().length() >= 6) {
			return false;
		}
		// Leading + or - is not allowed
		if (text.contains("-") || text.contains("+")) {
			return false;
		}
		// Text must be a valid integer
		try {
			Integer.parseInt(text);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/* 
	 * Invoked prior to replacing a region of text in the specified Document.
	 */
	@Override
	public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) {
		Document doc = fb.getDocument();
		StringBuilder sb = new StringBuilder();
		try {
			sb.append(doc.getText(0, doc.getLength()));
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		sb.replace(offset, offset + length, text);

		if (test(sb.toString())) {
			try {
				super.replace(fb, offset, length, text, attrs);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

	/* 
	 * Invoked prior to removal of the specified region in the specified Document.
	 */
	@Override
	public void remove(FilterBypass fb, int offset, int length) {
		Document doc = fb.getDocument();
		StringBuilder sb = new StringBuilder();
		try {
			sb.append(doc.getText(0, doc.getLength()));
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		sb.delete(offset, offset + length);

		if (test(sb.toString())) {
			try {
				super.remove(fb, offset, length);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}

	}
}