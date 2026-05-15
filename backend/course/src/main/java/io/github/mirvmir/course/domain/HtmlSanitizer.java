package io.github.mirvmir.course.domain;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

public final class HtmlSanitizer {

    private static final PolicyFactory POLICY =
            new HtmlPolicyBuilder()

                    .allowElements(
                            "p", "br",
                            "h2", "h3", "h4",
                            "strong", "b",
                            "em", "i", "u",
                            "ul", "ol", "li",
                            "blockquote",
                            "pre", "code",
                            "a",
                            "table", "thead",
                            "tbody", "tr",
                            "th", "td"
                    )

                    .allowAttributes("href")
                    .onElements("a")

                    .allowUrlProtocols(
                            "http",
                            "https",
                            "mailto"
                    )

                    .requireRelNofollowOnLinks()

                    .toFactory();

    private HtmlSanitizer() {
    }

    public static String sanitize(String html) {
        return POLICY.sanitize(html);
    }
}