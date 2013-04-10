/**
 * 
 */
package uk.ac.gla.dcs.explicator.vocabularies.client;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * @author agray
 *
 */
public class ErrorPopup extends PopupPanel {
	
	public ErrorPopup(String text) {
		super(true);
		
		HTML contents = new HTML(text);
		contents.setWidth("128px");
		setWidget(contents);
		setStyleName("error-Popup");
	}

}
