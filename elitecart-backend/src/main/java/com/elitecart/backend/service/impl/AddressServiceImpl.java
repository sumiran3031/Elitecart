package com.elitecart.backend.service.impl;

import com.elitecart.backend.dto.address.AddressRequest;
import com.elitecart.backend.dto.address.AddressResponse;
import com.elitecart.backend.entity.Address;
import com.elitecart.backend.entity.User;
import com.elitecart.backend.exception.BadRequestException;
import com.elitecart.backend.exception.ResourceNotFoundException;
import com.elitecart.backend.mapper.AddressMapper;
import com.elitecart.backend.repository.AddressRepository;
import com.elitecart.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements com.elitecart.backend.service.AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    @Override
    @Transactional
    public AddressResponse create(Long userId, AddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Address address = buildAddress(request, user);

        if (address.isDefault()) {
            clearExistingDefault(userId, address.getAddressType());
        }

        Address saved = addressRepository.save(address);
        return addressMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AddressResponse update(Long userId, Long addressId, AddressRequest request) {
        Address address = getOwnedAddress(userId, addressId);

        address.setFullName(request.getFullName());
        address.setPhoneNumber(request.getPhoneNumber());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setAddressType(parseType(request.getAddressType()));
        address.setDefault(request.isDefault());

        if (request.isDefault()) {
            clearExistingDefault(userId, address.getAddressType());
        }

        Address saved = addressRepository.save(address);
        return addressMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long addressId) {
        Address address = getOwnedAddress(userId, addressId);
        addressRepository.delete(address);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getAllForUser(Long userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(addressMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getById(Long userId, Long addressId) {
        return addressMapper.toResponse(getOwnedAddress(userId, addressId));
    }

    private Address buildAddress(AddressRequest request, User user) {
        return Address.builder()
                .user(user)
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .addressType(parseType(request.getAddressType()))
                .isDefault(request.isDefault())
                .build();
    }

    private Address.AddressType parseType(String type) {
        try {
            return Address.AddressType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException("Address type must be SHIPPING or BILLING");
        }
    }

    private void clearExistingDefault(Long userId, Address.AddressType type) {
        addressRepository.findByUserId(userId).stream()
                .filter(a -> a.isDefault() && a.getAddressType() == type)
                .forEach(a -> {
                    a.setDefault(false);
                    addressRepository.save(a);
                });
    }

    private Address getOwnedAddress(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));
        if (!address.getUser().getId().equals(userId)) {
            throw new BadRequestException("This address does not belong to the current user");
        }
        return address;
    }
}
