package core.heal.ai;

import java.util.List;

public interface AiClient {

    /**
     * Génère un locator Playwright sous format standard :
     * css|selector
     * xpath|selector
     * text|text
     * label|label
     * placeholder|placeholder
     * role|role|name
     */
    List<String> generateLocator(String prompt);
}
