package com.example.demo.web;

import com.example.demo.entity.Customer;
import com.example.demo.dto.CustomerDTO;
import com.example.demo.services.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<?> createCustomer(@Valid @RequestBody Customer customer, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            // Збірка всіх повідомлень про помилки у валідації полів
            List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }

        Optional<Customer> existingCustomer = customerService.getCustomerByEmail(customer.getEmail());
        if (existingCustomer.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Customer with email " + customer.getEmail() + " already exists");
        }

        // Створення нового користувача
        Customer savedCustomer = customerService.createCustomer(customer);
        CustomerDTO response = new CustomerDTO(
                savedCustomer.getId(),
                savedCustomer.getFullName(),
                savedCustomer.getEmail(),
                savedCustomer.getPhone()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<CustomerDTO> readAllCustomers() {
        return customerService.getAllCustomers().stream()
                .map(customer -> new CustomerDTO(
                        customer.getId(),
                        customer.getFullName(),
                        customer.getEmail(),
                        customer.getPhone()
                ))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> readCustomer(@PathVariable Long id) {
        Optional<Customer> optionalCustomer = customerService.getCustomerById(id);

        if (optionalCustomer.isPresent()) {
            Customer customer = optionalCustomer.get();
            CustomerDTO response = new CustomerDTO(
                    customer.getId(),
                    customer.getFullName(),
                    customer.getEmail(),
                    customer.getPhone()
            );
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCustomer(@Valid @PathVariable Long id, @RequestBody Customer customer, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }

        Customer existingCustomer = customerService.getCustomerById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        customer.setEmail(existingCustomer.getEmail());

        Customer updatedCustomer = customerService.updateCustomer(id, customer);
        CustomerDTO response = new CustomerDTO(
                updatedCustomer.getId(),
                updatedCustomer.getFullName(),
                updatedCustomer.getEmail(),
                updatedCustomer.getPhone()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
