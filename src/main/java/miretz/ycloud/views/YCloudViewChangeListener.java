package miretz.ycloud.views;

import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.UI;

public class YCloudViewChangeListener implements ViewChangeListener {

	protected UI mainUi;

	public YCloudViewChangeListener(UI mainUi) {
		this.mainUi = mainUi;
	}

	private static final long serialVersionUID = 1L;

	@Override
	public boolean beforeViewChange(ViewChangeEvent event) {

		boolean isLoggedIn = mainUi.getSession().getAttribute("user") != null;
		boolean isLoginView = event.getNewView() instanceof LoginView;

		if (!isLoggedIn && !isLoginView) {
			mainUi.getNavigator().navigateTo(LoginView.NAME);
			return false;
		} else if (isLoggedIn && isLoginView) {
			return false;
		}
		return true;
	}

	@Override
	public void afterViewChange(ViewChangeEvent event) {

	}

}
