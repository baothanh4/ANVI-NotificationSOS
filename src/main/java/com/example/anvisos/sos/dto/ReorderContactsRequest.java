package com.example.anvisos.sos.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReorderContactsRequest {
    @NotEmpty
    private List<Long> orderedContactIds;
}

