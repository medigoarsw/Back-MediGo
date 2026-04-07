package edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.in.dto;

import edu.escuelaing.arsw.medigo.auction.domain.port.in.QueryAuctionUseCase;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record WonAuctionsPageResponse(
        List<WonAuctionResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        Map<String, String> links
) {
    public static WonAuctionsPageResponse from(QueryAuctionUseCase.WonAuctionsPageView pageView,
                                               String basePath) {
        List<WonAuctionResponse> content = pageView.content().stream()
                .map(WonAuctionResponse::from)
                .toList();

        Map<String, String> links = buildLinks(pageView, basePath);

        return new WonAuctionsPageResponse(
                content,
                pageView.page(),
                pageView.size(),
                pageView.totalElements(),
                pageView.totalPages(),
                links
        );
    }

    private static Map<String, String> buildLinks(QueryAuctionUseCase.WonAuctionsPageView pageView,
                                                  String basePath) {
        Map<String, String> links = new LinkedHashMap<>();
        links.put("self", buildPageUrl(basePath, pageView.page(), pageView.size()));

        if (pageView.page() > 0) {
            links.put("prev", buildPageUrl(basePath, pageView.page() - 1, pageView.size()));
        }

        if (pageView.page() + 1 < pageView.totalPages()) {
            links.put("next", buildPageUrl(basePath, pageView.page() + 1, pageView.size()));
        }

        return links;
    }

    private static String buildPageUrl(String basePath, int page, int size) {
        return basePath + "?page=" + page + "&size=" + size;
    }
}
