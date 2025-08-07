package com.laklu.pos.dataObjects.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private PaginationInfo pagination;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaginationInfo {
        private int pageNumber;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
    }
    
    public static <T, U> PageResponse<U> fromPage(Page<T> page, List<U> mappedContent) {
        PaginationInfo paginationInfo = PaginationInfo.builder()
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
        
        return PageResponse.<U>builder()
                .content(mappedContent)
                .pagination(paginationInfo)
                .build();
    }
} 