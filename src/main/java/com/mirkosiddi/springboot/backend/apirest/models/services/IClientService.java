package com.mirkosiddi.springboot.backend.apirest.models.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mirkosiddi.springboot.backend.apirest.models.entity.Client;

public interface IClientService {

	public List<Client> findAll();

	public Page<Client> findAll(Pageable pageRequest);

	public Client save(Client client);

	public Client findById(Long id);

	public void delete(Long id);

}
