package com.ksv.tactive.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedResponse<T> {
	private List<T> data;
	private int currentPage;
	private int pageSize;
	private long totalItems;
	private int totalPages;
}
