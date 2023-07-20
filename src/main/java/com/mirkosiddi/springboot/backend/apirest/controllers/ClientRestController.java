package com.mirkosiddi.springboot.backend.apirest.controllers;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mirkosiddi.springboot.backend.apirest.models.entity.Client;
import com.mirkosiddi.springboot.backend.apirest.models.services.IClientService;

import jakarta.validation.Valid;

@CrossOrigin(origins = { "http://localhost:4200" })
@RestController
@RequestMapping("/api")
public class ClientRestController {

	@Autowired
	private IClientService clienteService;

	@GetMapping("/clients")
	public List<Client> index() {
		return clienteService.findAll();
	}

	@GetMapping("/clients/page/{page}")
	public Page<Client> index(@PathVariable Integer page) {
		PageRequest pageable = PageRequest.of(page - 1, 4);
		return clienteService.findAll(pageable);
	}

	@GetMapping("/clients/{id}")
	public ResponseEntity<?> show(@PathVariable Long id) {
		Client cliente = null;
		Map<String, Object> response = new HashMap<>();
		try {
			cliente = this.clienteService.findById(id);
		} catch (DataAccessException e) {
			response.put("message", "Error al realizar la consulta");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		if (cliente == null) {
			response.put("message", "El cliente ID: ".concat(id.toString().concat(" no existe en la base de datos")));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Client>(cliente, HttpStatus.OK);
	}

	@PostMapping("/clients")
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<?> create(@Valid @RequestBody Client cliente, BindingResult result) {
		Client clienteNew = null;
		Map<String, Object> response = new HashMap<>();
		if (result.hasErrors()) {
			System.out.println("Errors");
			List<String> errors = result.getFieldErrors().stream()
					.map(err -> "Field " + err.getField() + "' " + err.getDefaultMessage())
					.collect(Collectors.toList());
			response.put("errors", errors);
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.BAD_REQUEST);
		}
		try {
			clienteNew = this.clienteService.save(cliente);
		} catch (DataAccessException e) {
			response.put("message", "Error inserting client in DDBB");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		response.put("message", "Client has been successfully created");
		response.put("cliente", clienteNew);
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
	}

	@PutMapping("/clients/{id}")
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<?> update(@RequestBody Client cliente, @PathVariable Long id) {
		Client currentCliente = null;
		Map<String, Object> response = new HashMap<>();
		try {
			currentCliente = this.clienteService.findById(id);
		} catch (DataAccessException e) {
			response.put("message", "Error modifying client in DDBB");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		if (currentCliente == null) {
			response.put("message", "Client ID: ".concat(id.toString().concat(" does not exists in DDBB")));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
		}
		currentCliente.setName(cliente.getName());
		currentCliente.setLastName(cliente.getLastName());
		currentCliente.setEmail(cliente.getEmail());
		currentCliente.setCreateAt(cliente.getCreateAt());
		try {
			this.clienteService.save(currentCliente);
		} catch (DataAccessException e) {
			response.put("message", "Error saving client in DDBB");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		response.put("message", "Client has been successfully modified");
		response.put("cliente", currentCliente);
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
	}

	@DeleteMapping("/clients/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ResponseEntity<?> delete(@PathVariable Long id) {
		Client currentCliente = null;
		Map<String, Object> response = new HashMap<>();
		try {
			currentCliente = this.clienteService.findById(id);
		} catch (DataAccessException e) {
			response.put("message", "Error finding client to delete in DDBB");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		if (currentCliente == null) {
			response.put("message", "Client ID: ".concat(id.toString().concat(" does not exists in DDBB")));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
		}
		try {
			this.clienteService.delete(id);
		} catch (DataAccessException e) {
			response.put("message", "Error deleting client from DDBB");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		response.put("message", "Client has been successfully deleted");
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
	}

	@PostMapping("clients/upload")
	public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file, @RequestParam("id") Long id) {
		Client currentCliente = null;
		Map<String, Object> response = new HashMap<>();
		try {
			currentCliente = this.clienteService.findById(id);
		} catch (DataAccessException e) {
			response.put("message", "Error finding client to delete in DDBB");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		if (!file.isEmpty()) {
			String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename().replace(" ", "");
			Path fileRoutePath = Paths.get("uploads").resolve(fileName).toAbsolutePath();
			try {
				Files.copy(file.getInputStream(), fileRoutePath);
			} catch (Exception e) {
				response.put("message", "Error uploading image " + fileName);
				response.put("error", e.getMessage().concat(": ").concat(e.getCause().getMessage()));
				return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			String prevFileName = currentCliente.getPhoto();
			if (prevFileName != null && prevFileName.length() > 0) {
				Path prevFileRoutePath = Paths.get("uploads").resolve(prevFileName).toAbsolutePath();
				File prevFile = prevFileRoutePath.toFile();
				if (prevFile.exists() && prevFile.canRead()) {
					prevFile.delete();
				}
			}
			currentCliente.setPhoto(fileName);
			clienteService.save(currentCliente);
			response.put("cliente", currentCliente);
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
		} else {
			response.put("message", "Image cannot be empty");
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/uploads/img/{fileName:.+}")
	public ResponseEntity<Resource> viewImage(@PathVariable String fileName) {
		Path fileRoutePath = Paths.get("uploads").resolve(fileName).toAbsolutePath();
		Resource resource = null;
		try {
			resource = new UrlResource(fileRoutePath.toUri());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!resource.exists() && !resource.isReadable()) {
			throw new RuntimeException("Error, file could not be loaded " + fileName);
		}
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename());

		return new ResponseEntity<Resource>(resource, headers, HttpStatus.OK);
	}
}
