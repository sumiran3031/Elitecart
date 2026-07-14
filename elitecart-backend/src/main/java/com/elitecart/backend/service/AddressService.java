package com.elitecart.backend.service;

import com.elitecart.backend.dto.address.AddressRequest;
import com.elitecart.backend.dto.address.AddressResponse;

import java.util.List;

public interface AddressService {
    AddressResponse create(Long userId, AddressRequest request);
    AddressResponse update(Long userId, Long addressId, AddressRequest request);
    void delete(Long userId, Long addressId);
    List<AddressResponse> getAllForUser(Long userId);
    AddressResponse getById(Long userId, Long addressId);
}
