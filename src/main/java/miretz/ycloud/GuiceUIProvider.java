package miretz.ycloud;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UICreateEvent;
import com.vaadin.server.UIProvider;

@SuppressWarnings("serial")
public class GuiceUIProvider extends UIProvider {

	@Inject
	private Injector injector;

	@Override
	public YCloudUI createInstance(UICreateEvent event) {
		return injector.getInstance(YCloudUI.class);
	}

	@Override
	public Class<YCloudUI> getUIClass(UIClassSelectionEvent event) {
		return YCloudUI.class;
	}
}
