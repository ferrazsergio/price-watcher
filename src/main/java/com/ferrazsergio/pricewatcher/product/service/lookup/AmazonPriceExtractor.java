package com.ferrazsergio.pricewatcher.product.service.lookup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Order(1)
public class AmazonPriceExtractor implements UrlPriceExtractor {

    @Override
    public boolean canHandle(String url) {
        try {
            String host = URI.create(url).getHost();
            return host != null && host.contains("amazon.");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public BigDecimal extract(String url) throws Exception {
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (PriceWatcherBot)")
                .timeout(20000)
                .get();

        Optional<String> priceText =
                selectText(doc, "#corePriceDisplay_desktop_feature_div .a-offscreen")
                .or(() -> selectText(doc, "#apex_desktop .a-offscreen"))
                .or(() -> selectText(doc, ".apexPriceToPay .a-offscreen"))
                .or(() -> selectText(doc, "#price_inside_buybox"))
                .or(() -> selectText(doc, "span.a-price .a-offscreen"))
                .or(() -> selectText(doc, "#corePrice_feature_div .a-offscreen"))
                .or(() -> regexFindPrice(doc.outerHtml()));

        if (priceText.isEmpty()) {
            throw new IllegalStateException("Não foi possível extrair o preço (Amazon)");
        }
        return parseToBigDecimal(priceText.get());
    }

    private Optional<String> selectText(Document doc, String css) {
        Element el = doc.selectFirst(css);
        return Optional.ofNullable(el).map(Element::text).map(String::trim).filter(s -> !s.isBlank());
    }

    private Optional<String> regexFindPrice(String html) {
        Pattern p = Pattern.compile("(?:R\$\s*)?(\d{1,3}(?:[\.,]\d{3})*[\.,]\d{2})");
        Matcher m = p.matcher(new String(html.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        if (m.find()) return Optional.of(m.group(1));
        return Optional.empty();
    }

    private BigDecimal parseToBigDecimal(String raw) {
        String s = raw.replaceAll("[^0-9, .]", "").replace(" ", "").trim();
        if (s.isEmpty()) throw new IllegalArgumentException("Preço vazio");

        int lastComma = s.lastIndexOf(',');
        int lastDot = s.lastIndexOf('.');
        char decimalSep;
        if (lastComma == -1 && lastDot == -1) {
            // no decimal sep, treat as integer cents
            return new BigDecimal(s);
        } else if (lastComma > lastDot) {
            decimalSep = ',';
        } else {
            decimalSep = '.';
        }
        // remove thousands separators
        if (decimalSep == ',') {
            s = s.replace(".", "");
            s = s.replace(",", ".");
        } else {
            s = s.replace(",", "");
        }
        return new BigDecimal(s);
    }
}