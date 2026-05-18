package core.heal.ai;

import core.heal.locator.LocatorDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AiPromptBuilder {

    private static final Logger logger = LoggerFactory.getLogger(AiPromptBuilder.class);

    private AiPromptBuilder() {
        // Utility class
    }

    public static String buildLocatorPrompt(
            String elementName,
            LocatorDefinition failedLocator,
            String simplifiedDom
    ) {
        String failedLocatorText = failedLocator != null
                ? failedLocator.toStorageFormat()
                : "unknown";

        String prompt = """
                You are a QA automation expert specialized in Playwright Java.
                
                The locator of this UI element failed.
                
                Element name:
                %s
                
                Failed locator:
                %s
                
                Simplified DOM:
                %s
                
                Your task:
                Generate exactly 4 stable locator candidates for this element.
                
                Accepted formats ONLY:
                    css|selector
                    xpath|selector
                    text|visible text
                    label|label text
                    placeholder|placeholder text
                    role|role|accessible name
                
                Rules:
                    - Return exactly 4 locators.
                    - Return one locator per line.
                    - Do not add explanations.
                    - Do not add markdown.
                    - Do not use numbering.
                    - Prefer stable attributes: id, name, data-testid, aria-label.
                    - Avoid fragile full XPath.
                    - Each result must be directly parseable by the framework.
                
                Expected output example:
                css|#input-email
                css|input[name='email']
                placeholder|E-Mail Address
                role|textbox|E-Mail Address
                """.formatted(
                elementName,
                failedLocatorText,
                simplifiedDom
        );

        logger.debug("[AI-PROMPT] Prompt generated for element: {}", elementName);

        return prompt;
    }
}
