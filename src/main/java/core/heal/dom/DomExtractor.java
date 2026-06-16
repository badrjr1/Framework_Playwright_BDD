package core.heal.dom;

import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DomExtractor {

    private static final Logger logger = LoggerFactory.getLogger(DomExtractor.class);

    private final Page page;

    public DomExtractor(Page page) {
        this.page = page;
        logger.debug("[DOM-EXTRACTOR] DomExtractor initialized");
    }

    public String extractSimplifiedDom() {
        try {
            logger.info("[DOM-EXTRACTOR] Extracting simplified DOM");

            String script = """
                    () => {
                        const allowedTags = ['input', 'button', 'a', 'select', 'textarea', 'label'];
                        const elements = Array.from(document.querySelectorAll(allowedTags.join(',')));

                        return elements.map(el => {
                            const tag = el.tagName.toLowerCase();

                            const attrs = {
                                tag: tag,
                                id: el.getAttribute('id'),
                                name: el.getAttribute('name'),
                                type: el.getAttribute('type'),
                                value: el.getAttribute('value'),
                                placeholder: el.getAttribute('placeholder'),
                                ariaLabel: el.getAttribute('aria-label'),
                                role: el.getAttribute('role'),
                                title: el.getAttribute('title'),
                                href: el.getAttribute('href'),
                                text: el.innerText ? el.innerText.trim() : ''
                            };

                            return Object.entries(attrs)
                                .filter(([key, value]) => value !== null && value !== '')
                                .map(([key, value]) => `${key}="${value}"`)
                                .join(' ');
                        }).join('\\n');
                    }
                    """;

            Object result = page.evaluate(script);

            String simplifiedDom = result != null ? result.toString() : "";

            if (simplifiedDom.length() > 8000) {
                logger.warn(
                        "[DOM-EXTRACTOR] DOM too large. Truncating from {} chars to 8000 chars",
                        simplifiedDom.length()
                );

                simplifiedDom = simplifiedDom.substring(0, 8000);
            }

            logger.debug("[DOM-EXTRACTOR] Simplified DOM extracted:\n{}", simplifiedDom);

            return simplifiedDom;

        } catch (Exception e) {
            logger.error("[DOM-EXTRACTOR] Failed to extract simplified DOM", e);
            throw new RuntimeException("Erreur lors de l'extraction du DOM simplifié", e);
        }
    }

    public String extractDomAroundKeyword(String keyword) {
        try {
            logger.info("[DOM-EXTRACTOR] Extracting DOM around keyword: {}", keyword);

            String simplifiedDom = extractSimplifiedDom();

            if (keyword == null || keyword.trim().isEmpty()) {
                return simplifiedDom;
            }

            String lowerKeyword = keyword.toLowerCase();

            StringBuilder result = new StringBuilder();

            String[] lines = simplifiedDom.split("\\R");

            for (String line : lines) {
                if (line.toLowerCase().contains(lowerKeyword)) {
                    result.append(line).append(System.lineSeparator());
                }
            }

            if (result.isEmpty()) {
                logger.warn(
                        "[DOM-EXTRACTOR] No DOM line found around keyword: {}. Returning full simplified DOM",
                        keyword
                );

                return simplifiedDom;
            }

            logger.debug("[DOM-EXTRACTOR] DOM around keyword extracted:\n{}", result);

            return result.toString();

        } catch (Exception e) {
            logger.error("[DOM-EXTRACTOR] Failed to extract DOM around keyword: {}", keyword, e);
            throw new RuntimeException("Erreur lors de l'extraction du DOM autour du mot-clé", e);
        }
    }
}